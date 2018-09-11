package com.ul.ims.apdu.interpreter.PresentationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.*;
import com.ul.ims.apdu.encoding.enums.FileControlInfo;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduFileException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.Exceptions.ResponseApduStatusCodeError;
import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayer;

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

    public Promise selectDF(DedicatedFileID fileID) {
        SelectCommand command = new SelectCommand();
        command.setFileControlInfo(FileControlInfo.NOFCIReturn);
        command.setFileID(fileID);

        return Promise.resolve(selectedDF == fileID)
                .then(isAlreadySelected -> {
                    if (isAlreadySelected) {
                        return Promise.resolve(new ResponseApdu().setStatusCode(StatusCode.SUCCESSFUL_PROCESSING));
                    }
                    return this.sessionLayer.send(command);
                })
                .then(this::verifySelectResponse)
                .then(result -> {
                    selectedDF = fileID;
                    return Promise.resolve(result);
                });
    }

    public Promise selectEF(final ElementaryFileID fileID) {
        SelectCommand command = new SelectCommand();
        command.setFileControlInfo(FileControlInfo.NOFCIReturn);
        command.setFileID(fileID);

        return Promise.resolve(this.selectedEF == fileID)
                .then(isAlreadySelected -> {
                    if (isAlreadySelected) {
                        return Promise.resolve(new ResponseApdu().setStatusCode(StatusCode.SUCCESSFUL_PROCESSING));
                    }
                    return this.sessionLayer.send(command);
                })
                .then(this::verifySelectResponse)
                .then(result -> {
                    this.selectedEF = fileID;
                    return Promise.resolve(result);
                });
    }

    private Promise<ResponseApdu> verifySelectResponse(ResponseApdu response) {
        return new Promise<>(settlement -> {
            if (response.getStatusCode() == StatusCode.SUCCESSFUL_PROCESSING) {
                settlement.resolve(response);
            } else {
                settlement.reject(new ResponseApduStatusCodeError(response.getStatusCode()));
            }
        });
    }

    /**
     * Routes call to right EF read. If short it'll use the short id otherwise it'll use the normal.
     * @param fileID
     * @return
     */
    public Promise<byte[]> readEF(ElementaryFileID fileID) {
        return selectApduFile(fileID).then((file) -> this.resolveApduFile(file));
    }

    /**
     * Creates the intial first part of a APDU file by selecting the ElementaryFileID on at the holder and read a few initial bytes.
     * @param fileID
     * @return
     */
    private Promise<ApduFile> selectApduFile(ElementaryFileID fileID) {
        return new Promise<>(settlement -> {
            ApduFile result;
            Promise<byte[]> promise;
            final byte offset = (byte)0;
            //If short file id is available, a read will also instantly select the file.
            if (fileID.isShortIDAvailable()) {
                promise = this.readEFShortID(fileID, offset);
            } else {
                promise = this.selectEF(fileID).then((v) -> readSelectedEF(offset));//Select and read the first part.
            }
            try {
                byte[] data = promise.getValue();
                result = new ApduFile(data);
            } catch (Throwable e) {
                settlement.reject(e);
                return;
            }
            settlement.resolve(result);
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
        return this.sessionLayer.send(command).then(this::verifyReadBinaryResponse);
    }

    //Read contents of EF using normalID.
    //The EF must first be selected.
    private Promise<byte[]> readSelectedEF(short offset) {
        ReadBinaryOffsetCommand command = new ReadBinaryOffsetCommand();
        command.setOffset(offset);
        command.setMaximumExpectedLength(maxExpLength);

        return this.sessionLayer.send(command).then(this::verifyReadBinaryResponse);
    }

    private Promise<byte[]> verifyReadBinaryResponse(ResponseApdu result) {
        StatusCode resultStatus = result.getStatusCode();
        return new Promise<>(settlement -> {
            if(resultStatus == StatusCode.SUCCESSFUL_PROCESSING || resultStatus == StatusCode.WARNING_END_OF_FILE) {
                settlement.resolve(result.getData());
                return;
            }
            settlement.reject(new ResponseApduStatusCodeError(resultStatus));
        });
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
