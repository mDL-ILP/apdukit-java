package com.ul.ims.apdu.interpreter.presentationLayer;

import com.ul.ims.apdu.encoding.ReadBinaryCommand;
import com.ul.ims.apdu.encoding.ReadBinaryShortFileIDCommand;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.SelectCommand;
import com.ul.ims.apdu.encoding.enums.FileType;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.encoding.types.FileID;
import com.ul.ims.apdu.interpreter.sessionLayer.HolderSessionLayer;

import java.util.Arrays;

/**
 *  The handle apdu protocol presentation layer. Extends the base apdu protocol layer with methods to know of to handle requests
 */
public class HolderPresentation implements HolderPresentationLayer {
    private HolderPresentationLayerDelegate delegate;
    private HolderSessionLayer sessionLayer;
    private ElementaryFileID selectedEF;
    private DedicatedFileID selectedDF;

    public HolderPresentation(HolderSessionLayer sessionLayer) {
        this.sessionLayer = sessionLayer;
        this.sessionLayer.setDelegate(this);
    }

    @Override
    public ResponseApdu receivedSelectCommand(SelectCommand command) {
        FileType type = command.getFileType();
        FileID requestedFileId = command.getFileID();
        boolean success = false;
        switch (type) {
            case DF:
                success = setSelectedDF((DedicatedFileID) requestedFileId);
                break;
            case EF:
                success = setSelectedEF((ElementaryFileID) requestedFileId);
                break;
        }
        return new ResponseApdu().setStatusCode(success ? StatusCode.SUCCESSFUL_PROCESSING : StatusCode.ERROR_FILE_NOT_FOUND);
    }

    public StatusCode checkPermissionForFile(ElementaryFileID id) {
        ApduFile file = this.delegate.getLocalFile(id);
        if (file == null) {
            return StatusCode.ERROR_FILE_NOT_FOUND;
        }
        //Check access
        if (delegate != null && !delegate.isFileAllowed(id)) {
            return StatusCode.ERROR_SECURITY_STATUS_NOT_SATISFIED;
        }
        return null;
    }

    @Override
    public ResponseApdu receivedReadCommand(ReadBinaryCommand command) {
        ElementaryFileID id = this.selectedEF;
        if (command instanceof ReadBinaryShortFileIDCommand) {
            id = ((ReadBinaryShortFileIDCommand) command).getElementaryFileID();
            this.selectedEF = id;
        }
        ApduFile file = this.delegate.getLocalFile(id);
        if (file == null) {
            return new ResponseApdu().setStatusCode(StatusCode.ERROR_FILE_NOT_FOUND);
        }
        if (delegate == null || !delegate.isFileAllowed(id)) {
            return new ResponseApdu().setStatusCode(StatusCode.ERROR_SECURITY_STATUS_NOT_SATISFIED);
        }
        return buildResponseOnRead(file, command.getOffset(), command.getMaximumExpectedLength());
    }

    private ResponseApdu buildResponseOnRead(ApduFile file, short offset, int maximumExpectedLength) {
        byte[] data = file.getData();
        int readStartIndex = offset;
        int readEndIndex = offset + maximumExpectedLength;//EndOffset is the index of how far we'll read.
        boolean askedForToomuch = readEndIndex > data.length;
        if(readStartIndex > data.length) {
            readStartIndex = (short) data.length;
        }
        if (askedForToomuch) {//Cap it to the length of the data.
            readEndIndex = data.length;
        }
        //If the given offset is too high or there is nothing to send back.
        if (readStartIndex >= readEndIndex) {
            return new ResponseApdu().setStatusCode(StatusCode.ERROR_COMMAND_NOT_ALLOWED);
        }
        byte[] payload = Arrays.copyOfRange(data, readStartIndex, readEndIndex);
        return new ResponseApdu()
            .setStatusCode(askedForToomuch ? StatusCode.WARNING_END_OF_FILE : StatusCode.SUCCESSFUL_PROCESSING)
            .setData(payload);
    }

    /**
     * Sets the selected app id (DedicatedFileID)and returns if this was successful.
     *
     * @param id dedicatedFileID specifying the app id
     * @return boolean indicating if the set was successful.
     */
    private boolean setSelectedDF(DedicatedFileID id) {
        if (id == null || !id.equals(this.delegate.getAppId())) {
            return false;
        }
        this.selectedDF = id;
        return true;
    }

    /**
     * Set the selected file id (elementaryFileID) and returns if this was successful.
     *
     * @param id elementaryFileID specifying the file
     * @return boolean indicating if the set was successful.
     */
    private boolean setSelectedEF(ElementaryFileID id) {
        //You can only select an EF after you've selected an app.
        if(selectedDF == null) {
            return false;
        }
        if (id == null || this.delegate.getLocalFile(id) == null) {
            return false;
        }
        this.selectedEF = id;
        return true;
    }

    /// Informs the delegate when got an exception when sending has failed.
    @Override
    public void onSendFailure(Exception exception) {
        this.delegate.onSendFailure(exception);
    }

    /// Informs the delegate when we've received an invalid apdu
    @Override
    public void onReceiveInvalidApdu(ParseException exception) {
        this.delegate.onReceiveInvalidApdu(exception);
    }

    @Override
    public void onEvent(String string, int i) {
        this.delegate.onEvent(string, i);
    }

    @Override
    public void setDelegate(PresentationLayerDelegate delegate) {
        this.delegate = (HolderPresentationLayerDelegate) delegate;
    }
}
