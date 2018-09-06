package com.ul.ims.apdu.interpreter.PresentationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.CommandApdu;
import com.ul.ims.apdu.encoding.Constants;
import com.ul.ims.apdu.encoding.ReadBinaryCommand;
import com.ul.ims.apdu.encoding.ReadBinaryOffsetCommand;
import com.ul.ims.apdu.encoding.ReadBinaryShortFileIDCommand;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.SelectCommand;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.encoding.enums.FileControlInfo;
import com.ul.ims.apdu.encoding.enums.SelectFileType;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduFileException;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.types.FileID;
import com.ul.ims.apdu.interpreter.Exceptions.ResponseApduStatusCodeError;
import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayer;
import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayerDelegate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class SimpleApduPresentationLayer implements PresentationLayer, SessionLayerDelegate {
    private SessionLayer sessionLayer;
    //State
    private DedicatedFileID selectedApp;
    private ElementaryFileID selectedEF;
    private HashMap<Short, ApduFile> files = new HashMap<>();

    private int maxExpLength = Constants.DEFAULT_MAX_EXPECTED_LENGTH_NOT_EXTENDED;
    Semaphore getFileLock = new Semaphore(1);

    private PresentationLayerDelegate delegate;
    //Config
    private DedicatedFileID appId;//app id

    public SimpleApduPresentationLayer(SessionLayer sessionLayer, DedicatedFileID appId) {
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

    private Promise<byte[]> buildCommandApdu(CommandApdu input) {
        return new Promise<>(settlement -> {
            try {
                settlement.resolve(input.toBytes().toByteArray());
            } catch (Exception e) {
                settlement.reject(e);
            }
        });
    }

    private Promise<ResponseApdu> send(CommandApdu command) {
        return buildCommandApdu(command).then(bytes -> sessionLayer.send(bytes));
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

    private Promise<ResponseApdu> verifySelectResponse(ResponseApdu response) {
        return new Promise<>(settlement -> {
            if (response.getStatusCode() == StatusCode.SUCCESSFUL_PROCESSING) {
                settlement.resolve(response);
            } else {
                settlement.reject(new ResponseApduStatusCodeError(response.getStatusCode()));
            }
        });
    }

    /*
    Routes call to right EF read. If short it'll use the short id otherwise it'll use the normal.
     */
    private Promise<byte[]> readEF(ElementaryFileID fileID) {
        return selectApduFile(fileID).then((file) -> this.resolveApduFile(file));
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
     * Sets the file for a particular file id on the holders side.
     *
     * @param id   the file id of the data
     * @param data bytes of the file.
     * @return boolean informing the caller if the file was successfully set.
     */
    @Override
    public boolean setFile(ElementaryFileID id, byte[] data) {
        ApduFile file;
        try {
            file = new ApduFile(data);
            if (!file.isComplete()) {
                return false;
            }
            files.put(id.getNormalIDValueAsAShort(), file);
        } catch (Exception e) {
            return false;
        }

        try {
            files.put(id.getShortIDValueAsAShort(), file);
        } catch (InvalidNumericException e) {
        }
        return true;
    }

    @Override
    public void setDelegate(PresentationLayerDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * Handles a select request.
     *
     * @param command
     * @return
     */
    @Override
    public ResponseApdu receivedSelectRequest(SelectCommand command) {
        SelectFileType type = command.getFileType();
        StatusCode result = StatusCode.ERROR_UNKNOWN;
        FileID requestedFileId = command.getFileID();
        switch (type) {
            case DF:
                result = setSelectedApp((DedicatedFileID) requestedFileId) ? StatusCode.SUCCESSFUL_PROCESSING : StatusCode.ERROR_FILE_NOT_FOUND;
                break;
            case EF:
                result = setSelectedEF((ElementaryFileID) requestedFileId) ? StatusCode.SUCCESSFUL_PROCESSING : StatusCode.ERROR_FILE_NOT_FOUND;
                break;
        }
        return new ResponseApdu().setStatusCode(result);
    }

    @Override
    public void onReceiveInvalidApdu(ParseException exception) {
        exception.printStackTrace();
    }

    @Override
    public void onSendFailure(Exception exception) {

    }

    /**
     * Handles a read request.
     *
     * @param command
     * @return
     */
    @Override
    public ResponseApdu receivedReadRequest(ReadBinaryCommand command) {
        ResponseApdu response = new ResponseApdu().setStatusCode(StatusCode.ERROR_UNKNOWN);

        ElementaryFileID id = this.selectedEF;
        if (command instanceof ReadBinaryShortFileIDCommand) {
            id = ((ReadBinaryShortFileIDCommand) command).getElementaryFileID();
        }
        if (id == null) {
            response.setStatusCode(StatusCode.ERROR_COMMAND_NOT_ALLOWED);
            return response;
        }
        //Check if file exists
        ApduFile file = getLocalFile(id);
        if (file == null) {
            response.setStatusCode(StatusCode.ERROR_FILE_NOT_FOUND);
            return response;
        }
        //Check access
        if (delegate != null && !delegate.checkAccessConditions(id)) {
            response.setStatusCode(StatusCode.ERROR_SECURITY_STATUS_NOT_SATISFIED);
            return response;
        }
        this.selectedEF = id;//On read we also set the selectEF. For the case of a ReadBinaryShortFileIDCommand where we don't select before read.

        Short readBeginIndex = command.getOffset();
        Short maxResponseSize = (short) command.getMaximumExpectedLength();
        byte[] data = file.getData();

        //Calculate readEndIndex and cap it to the length of the data.
        //EndOffset is the index of how far we'll read.
        int readEndIndex = readBeginIndex + maxResponseSize;
        boolean askedForToomuch = false;
        if (readEndIndex > data.length) {
            readEndIndex = data.length;
            askedForToomuch = true;
        }

        //If the given readBeginIndex is too high or there is nothing to send back.
        if (readBeginIndex >= readEndIndex) {
            response.setStatusCode(StatusCode.ERROR_COMMAND_NOT_ALLOWED);
            return response;
        }
        response.setStatusCode(askedForToomuch ? StatusCode.WARNING_END_OF_FILE : StatusCode.SUCCESSFUL_PROCESSING);
        response.setData(Arrays.copyOfRange(data, readBeginIndex, readEndIndex));
        return response;
    }

    /**
     * Extended length. Set max expected length for response to read commands.
     *
     * @param value new max expected length value
     */
    public void setMaximumExpectedLength(short value) {
        this.maxExpLength = value;
    }

    /**
     * get max expected length for response to read commands.
     *
     * @return maxExpLength
     */
    public int getMaximumExpectedLength() {
        return maxExpLength;
    }

    /**
     * Sets the selected app id (DedicatedFileID)and returns if this was successful.
     *
     * @param id dedicatedFileID specifying the app id
     * @return boolean indicating if the set was successful.
     */
    private boolean setSelectedApp(DedicatedFileID id) {
        if (id == null || !id.equals(appId)) {
            return false;
        }
        this.selectedApp = id;
        return true;
    }

    /**
     * Set the selected file id (elementaryFileID) and returns if this was successful.
     *
     * @param id elementaryFileID specifying the file
     * @return boolean indicating if the set was successful.
     */
    private boolean setSelectedEF(ElementaryFileID id) {
        if (id == null || this.getLocalFile(id) == null) {
            return false;
        }
        this.selectedEF = id;
        return true;
    }

    /**
     * Returns back a local file from this.files. Trying both short and normal.
     *
     * @param id elementaryFileID specifying the file
     * @return an ApduFile
     */
    private ApduFile getLocalFile(ElementaryFileID id) {
        try {
            short key = id.getShortIDValueAsAShort();
            if (files.containsKey(key)) {
                return files.get(key);
            }
        } catch (Exception ignored) {
        }
        try {
            short key = id.getNormalIDValueAsAShort();
            if (files.containsKey(key)) {
                return files.get(key);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
