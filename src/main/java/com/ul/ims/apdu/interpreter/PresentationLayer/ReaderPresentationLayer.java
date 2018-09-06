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
import com.ul.ims.apdu.interpreter.SessionLayer.ReaderSessionLayerDelegate;
import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayer;

import java.util.concurrent.Semaphore;

public class ReaderPresentationLayer implements PresentationLayer, ReaderSessionLayerDelegate {
    private SessionLayer sessionLayer;
    //State
    private DedicatedFileID selectedApp;
    private ElementaryFileID selectedEF;
    private int maxExpLength = Constants.DEFAULT_MAX_EXPECTED_LENGTH_NOT_EXTENDED;
    private Semaphore getFileLock = new Semaphore(1);

    private PresentationLayerDelegate delegate;
    //Config
    private DedicatedFileID appId;//app id

    public ReaderPresentationLayer(SessionLayer sessionLayer, DedicatedFileID appId) {
        this.sessionLayer = sessionLayer;
        this.sessionLayer.setDelegate(this);
        this.appId = appId;
    }

    /**
     * Gets an elementary file. Hangs if there is already an open request.
     *
     * @param elementaryFileID
     * @return
     */
    public Promise<byte[]> getFile(ElementaryFileID elementaryFileID) {
        try {
            getFileLock.acquire();
        } catch (InterruptedException e) {
            return Promise.reject(e);
        }
        return selectApp().then((res) -> {
            return readEF(elementaryFileID);
        }).always(() -> {
            getFileLock.release();
        });
    }

    private Promise selectApp() {
        return selectDF(this.appId);
    }

    /*
    Routes call to right EF read. If short it'll use the short id otherwise it'll use the normal.
     */
    private Promise<byte[]> readEF(ElementaryFileID fileID) {
        return selectApduFile(fileID).then(this::resolveApduFile);
    }

    /**
     * Creates the intial first part of a APDU file by selecting the ElementaryFileID on at the holder and read a few initial bytes.
     *
     * @param fileID
     * @return
     */
    private Promise<ApduFile> selectApduFile(ElementaryFileID fileID) {
        return new Promise<>(settlement -> {
            ApduFile result;
            Promise<byte[]> promise;
            final byte offset = (byte) 0;
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

    //Read contents of EF using normalID.
    //The EF must first be selected.
    private Promise<byte[]> readSelectedEF(short offset) {
        ReadBinaryOffsetCommand command = new ReadBinaryOffsetCommand();
        command.setOffset(offset);
        command.setMaximumExpectedLength(maxExpLength);

        return send(command).then(this::verifyReadBinaryResponse);
    }

    private Promise<byte[]> verifyReadBinaryResponse(ResponseApdu result) {
        StatusCode resultStatus = result.getStatusCode();
        return new Promise<>(settlement -> {
            if (resultStatus == StatusCode.SUCCESSFUL_PROCESSING || resultStatus == StatusCode.WARNING_END_OF_FILE) {
                settlement.resolve(result.getData());
                return;
            }
            settlement.reject(new ResponseApduStatusCodeError(resultStatus));
        });
    }

    /**
     * This method will take a complete or incomplete APDu file and keeps reading until it is complete. Then return the bytes.
     *
     * @param file APDU file. Created by reading the first 5 bytes or part of the file.\
     * @return
     */
    private Promise<byte[]> resolveApduFile(ApduFile file) {
        if (file == null) {
            return Promise.reject(new InvalidApduFileException("File is null"));
        }
        return new Promise<>(settlement -> {
            while (!file.isComplete()) {
                short offset = file.getCurrentSize();
                Promise<byte[]> promise = readSelectedEF(offset);
                try {
                    byte[] data = promise.getValue();
                    file.appendValue(data);
                } catch (Throwable e) {
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
        return send(command).then(this::verifyReadBinaryResponse);
    }

    private Promise selectDF(DedicatedFileID fileID) {
        SelectCommand command = new SelectCommand();
        command.setFileControlInfo(FileControlInfo.NOFCIReturn);
        command.setFileID(fileID);

        return Promise.resolve(selectedApp == appId)
                .then(isAlreadySelected -> {
                    if (isAlreadySelected) {
                        return Promise.resolve(new ResponseApdu().setStatusCode(StatusCode.SUCCESSFUL_PROCESSING));
                    }
                    return send(command);
                })
                .then(this::verifySelectResponse)
                .then(result -> {
                    selectedApp = fileID;
                    return Promise.resolve(result);
                });
    }

    private Promise selectEF(final ElementaryFileID fileID) {
        SelectCommand command = new SelectCommand();
        command.setFileControlInfo(FileControlInfo.NOFCIReturn);
        command.setFileID(fileID);

        return Promise.resolve(this.selectedEF == fileID)
                .then(isAlreadySelected -> {
                    if (isAlreadySelected) {
                        return Promise.resolve(new ResponseApdu().setStatusCode(StatusCode.SUCCESSFUL_PROCESSING));
                    }
                    return send(command);
                })
                .then(this::verifySelectResponse)
                .then(result -> {
                    this.selectedEF = fileID;
                    return Promise.resolve(result);
                });
    }

    private Promise<ResponseApdu> send(CommandApdu command) {
        return buildCommandApdu(command).then(bytes -> sessionLayer.send(bytes));
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

    private Promise<byte[]> buildCommandApdu(CommandApdu input) {
        return new Promise<>(settlement -> {
            try {
                settlement.resolve(input.toBytes().toByteArray());
            } catch (Exception e) {
                settlement.reject(e);
            }
        });
    }

    @Override
    public void setDelegate(PresentationLayerDelegate delegate) {
        this.delegate = delegate;
    }

//    /**
//     * Extended length. Set max expected length for response to read commands.
//     *
//     * @param value new max expected length value
//     */
//    @Override
//    public void setMaximumExpectedLength(int value) {
//        this.maxExpLength = value;
//    }
//
//    /**
//     * get max expected length for response to read commands.
//     *
//     * @return maxExpLength
//     */
//    @Override
//    public int getMaximumExpectedLength() {
//        return maxExpLength;
//    }

    @Override
    public void onReceiveInvalidApdu(ParseException exception) {
        exception.printStackTrace();
    }

    @Override
    public void onSendFailure(Exception exception) {
        exception.printStackTrace();
    }
}
