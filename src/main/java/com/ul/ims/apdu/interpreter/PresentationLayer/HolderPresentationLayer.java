package com.ul.ims.apdu.interpreter.PresentationLayer;

import com.ul.ims.apdu.encoding.*;
import com.ul.ims.apdu.encoding.enums.SelectFileType;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.encoding.types.FileID;
import com.ul.ims.apdu.interpreter.SessionLayer.HolderSessionLayerDelegate;
import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayer;

import java.util.Arrays;
import java.util.HashMap;

public class HolderPresentationLayer implements PresentationLayer, HolderSessionLayerDelegate {
    private SessionLayer sessionLayer;
    //State
    private DedicatedFileID selectedApp;
    private ElementaryFileID selectedEF;
    private HashMap<Short, ApduFile> files = new HashMap<>();

    private int maxExpLength = Constants.DEFAULT_MAX_EXPECTED_LENGTH_NOT_EXTENDED;

    private HolderPresentationLayerDelegate delegate;
    //Config
    private DedicatedFileID appId;//app id

    public HolderPresentationLayer(SessionLayer sessionLayer, DedicatedFileID appId) {
        this.sessionLayer = sessionLayer;
        this.sessionLayer.setDelegate(this);
        this.appId = appId;
    }

    /**
     * Sets the file for a particular file id on the holders side.
     *
     * @param id   the file id of the data
     * @param data bytes of the file.
     * @return boolean informing the caller if the file was successfully set.
     */
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
        this.delegate = (HolderPresentationLayerDelegate) delegate;
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

    @Override
    public void onReceiveInvalidApdu(ParseException exception) {
        exception.printStackTrace();
    }

    @Override
    public void onSendFailure(Exception exception) {
        exception.printStackTrace();
    }
}
