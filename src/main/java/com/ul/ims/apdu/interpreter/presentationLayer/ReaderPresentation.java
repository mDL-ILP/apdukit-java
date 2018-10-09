package com.ul.ims.apdu.interpreter.presentationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.*;
import com.ul.ims.apdu.encoding.enums.FileControlInfo;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduFileException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.exceptions.ResponseApduStatusCodeError;
import com.ul.ims.apdu.interpreter.sessionLayer.ReaderSessionLayer;
import com.ul.ims.apdu.interpreter.sessionLayer.SessionLayer;

/**
 * The base APDU protocol presentation layer. It keeps state of what DF and EF are selected. Exposes methods to select DF or EF.
 */
public class ReaderPresentation implements ReaderPresentationLayer {
    protected ReaderPresentationLayerDelegate delegate;
    protected ReaderSessionLayer sessionLayer;

    //State
    protected DedicatedFileID selectedDF;
    protected ElementaryFileID selectedEF;
    private int maxExpLength = Constants.DEFAULT_MAX_EXPECTED_LENGTH_NOT_EXTENDED;

    public ReaderPresentation(ReaderSessionLayer sessionLayer) {
        this.sessionLayer = sessionLayer;
    }

    /**
     * Select a dedicated file (app)
     * @param fileID
     * @return
     */
    public Promise selectDF(DedicatedFileID fileID) {
        if(selectedDF == fileID) {
            return Promise.resolve(null);
        }
        SelectCommand command = new SelectCommand()
                .setFileControlInfo(FileControlInfo.NOFCIReturn)
                .setFileID(fileID);

        return this.sessionLayer.send(command)
                .then(this::readSelectResponse)
                .then(result -> {
                    selectedDF = fileID;
                    return Promise.resolve(result);
                });
    }

    /**
     * Select an elementary file (file, datagroup)
     * @param fileID
     * @return
     */
    public Promise selectEF(final ElementaryFileID fileID) {
        if(this.selectedEF == fileID) {
            return Promise.resolve(null);
        }
        SelectCommand command = new SelectCommand()
                .setFileControlInfo(FileControlInfo.NOFCIReturn)
                .setFileID(fileID);

        return this.sessionLayer.send(command)
                .then(this::readSelectResponse)
                .then(result -> {
                    selectedEF = fileID;
                    return Promise.resolve(result);
                });
    }

    /**
     * Read contents of EF using shortID.
     * @param fileID specifying the file
     * @param offset the offset of the data. Cannot exceed the file size.
     * @return bytes of the file
     */
    public Promise<byte[]> readBinary(ElementaryFileID fileID, byte offset) {
        ReadBinaryShortFileIDCommand command = new ReadBinaryShortFileIDCommand()
                .setOffset(offset)
                .setElementaryFileID(fileID);
        command.setMaximumExpectedLength(maxExpLength);
        return this.sessionLayer.send(command).then(this::readReadBinaryResponse);
    }

    /**
     * Read contents of EF using normalID. The EF must first be selected.
     * @param offset the offset of the data. Cannot exceed the file size.
     * @return bytes of the file
     */
    public Promise<byte[]> readBinary(short offset) {
        ReadBinaryOffsetCommand command = new ReadBinaryOffsetCommand()
        .setOffset(offset);
        command.setMaximumExpectedLength(maxExpLength);
        return this.sessionLayer.send(command).then(this::readReadBinaryResponse);
    }

    /**
     * Reads the response APDU and returns a new promise that will be rejected if the status code is not SUCCESSFUL
     * @param response
     * @return promise with the response
     */
    private Promise<ResponseApdu> readSelectResponse(ResponseApdu response) {
        StatusCode statusCode = response.getStatusCode();
        if(statusCode == StatusCode.SUCCESSFUL_PROCESSING) {
            return Promise.resolve(response);
        } else {
            return Promise.reject(new ResponseApduStatusCodeError(statusCode));
        }
    }

    /**
     * Reads the response APDU and returns the data of the response
     * @param response
     * @return the binary data from the response
     */
    private Promise<byte[]> readReadBinaryResponse(ResponseApdu response) {
        StatusCode statusCode = response.getStatusCode();
        if(statusCode == StatusCode.SUCCESSFUL_PROCESSING || statusCode == StatusCode.WARNING_END_OF_FILE) {
            byte[] data = response.getData();
            if(data != null) {
                return Promise.resolve(data);
            }
            return Promise.reject(new Exception("No data in response"));
        } else {
            return Promise.reject(new ResponseApduStatusCodeError(statusCode));
        }
    }

    @Override
    public void setDelegate(PresentationLayerDelegate delegate) {
        this.delegate = (ReaderPresentationLayerDelegate) delegate;
    }

    @Override
    public void onSendFailure(Exception exception) {
        this.delegate.onSendFailure(exception);
    }

    @Override
    public void onReceiveInvalidApdu(ParseException exception) {
        this.delegate.onReceiveInvalidApdu(exception);
    }

    @Override
    public void onEvent(String string, int i) {
        this.delegate.onEvent(string, i);
    }
}
