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
import com.ul.ims.apdu.interpreter.sessionLayer.SessionLayer;

/**
 * The base APDU protocol presentation layer. It keeps state of what DF and EF are selected. Exposes methods to select DF or EF.
 */
abstract class BaseApduProtocolPresentationLayer implements PresentationLayer {
    protected PresentationLayerDelegate delegate;
    protected SessionLayer sessionLayer;

    //State
    protected DedicatedFileID selectedDF;
    protected ElementaryFileID selectedEF;
    private int maxExpLength = Constants.DEFAULT_MAX_EXPECTED_LENGTH_NOT_EXTENDED;

    public BaseApduProtocolPresentationLayer(SessionLayer sessionLayer) {
        this.sessionLayer = sessionLayer;
        this.sessionLayer.setDelegate(this);
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
     * Routes call to right EF read. If short it'll use the short id otherwise it'll use the normal.
     * @param fileID
     * @return
     */
    public Promise<byte[]> readEF(ElementaryFileID fileID) {
        return openApduFile(fileID).then((file) -> this.resolveApduFile(file));
    }

    /**
     * Creates the intial first part of a APDU file by selecting the ElementaryFileID on at the holder and read a few initial bytes.
     * @param fileID
     * @return
     */
    private Promise<ApduFile> openApduFile(ElementaryFileID fileID) {
        Promise<byte[]> promise;
        //If short file id is available, a read will also instantly select the file.
        if (fileID.isShortIDAvailable()) {
            promise = this.readEFShortID(fileID, (byte)0);
        } else {
            promise = this.selectEF(fileID).then((v) -> readSelectedEF((byte)0));//Select and read the first part.
        }
        return promise.then((data) -> {
            try {
                ApduFile result = new ApduFile(data);
                return Promise.resolve(result);
            }catch (Exception e) {
                return Promise.reject(e);
            }
        });
    }

    /**
     * This method will take a complete or incomplete APDu file and keeps reading until it is complete. Then return the bytes.
     * @param file APDU file. Created by reading the first 5 bytes or part of the file.\
     * @return
     */
    private Promise<byte[]> resolveApduFile(ApduFile file) {
        if(file == null) {
            return Promise.reject(new InvalidApduFileException("File is null"));
        }
        return new Promise<>(settlement -> {
            while (!file.isComplete()) {
                short offset = file.getCurrentSize();
                Promise<byte[]> promise = readSelectedEF(offset);
                try {
                    byte[] data = promise.getValue();
                    file.appendValue(data);
                }  catch (Throwable e) {
                    settlement.reject(e);
                }
            }
            settlement.resolve(file.getData());
        });
    }

    //Read contents of EF using shortID.
    private Promise<byte[]> readEFShortID(ElementaryFileID fileID, byte offset) {
        ReadBinaryShortFileIDCommand command = new ReadBinaryShortFileIDCommand();
        command.setOffset(offset);
        command.setElementaryFileID(fileID);
        command.setMaximumExpectedLength(maxExpLength);
        return this.sessionLayer.send(command).then(this::readReadBinaryResponse);
    }

    //Read contents of EF using normalID.
    //The EF must first be selected.
    private Promise<byte[]> readSelectedEF(short offset) {
        ReadBinaryOffsetCommand command = new ReadBinaryOffsetCommand();
        command.setOffset(offset);
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
        this.delegate = delegate;
    }

    @Override
    public void onSendFailure(Exception exception) {
        this.delegate.onSendFailure(exception);
    }

    @Override
    public void onReceiveInvalidApdu(ParseException exception) {
        this.delegate.onReceiveInvalidApdu(exception);
    }
}
