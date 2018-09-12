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
import com.ul.ims.apdu.interpreter.sessionLayer.SessionLayer;
import com.ul.ims.apdu.interpreter.sessionLayer.SessionLayerDelegate;

import java.util.Arrays;

/**
 *  The handle apdu protocol presentation layer. Extends the base apdu protocol layer with methods to know of to handle requests
 */
public class ApduProtocolPresentationLayer extends BaseApduProtocolPresentationLayer  implements SessionLayerDelegate {

    public ApduProtocolPresentationLayer(SessionLayer sessionLayer) {
        super(sessionLayer);
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

    @Override
    public ResponseApdu receivedReadCommand(ReadBinaryCommand command) {
        ElementaryFileID id = this.selectedEF;
        if (command instanceof ReadBinaryShortFileIDCommand) {
            id = ((ReadBinaryShortFileIDCommand) command).getElementaryFileID();
        }
        StatusCode fileIDPermissionState = null;
        //Check if file exists
        ApduFile file = this.delegate.getLocalFile(id);
        if (file == null) {
            fileIDPermissionState = StatusCode.ERROR_FILE_NOT_FOUND;
        }
        //Check access
        if (delegate != null && !delegate.isFileAllowed(id)) {
            fileIDPermissionState = StatusCode.ERROR_SECURITY_STATUS_NOT_SATISFIED;
        }
        if(fileIDPermissionState != null) {
            return new ResponseApdu().setStatusCode(StatusCode.ERROR_UNKNOWN).setStatusCode(fileIDPermissionState);
        }
        this.selectedEF = id;//On read we also set the selectEF. For the case of a ReadBinaryShortFileIDCommand where we don't select before read.
        return buildResponseOnRead(file, command.getOffset(), command.getMaximumExpectedLength());
    }

    private ResponseApdu buildResponseOnRead(ApduFile file, short offset, int maximumExpectedLength) {
        ResponseApdu response =  new ResponseApdu();

        byte[] data = file.getData();
        //Calculate readEndIndex and cap it to the length of the data.
        //EndOffset is the index of how far we'll read.
        int readEndIndex = offset + maximumExpectedLength;
        boolean askedForToomuch = false;
        if (readEndIndex > data.length) {
            readEndIndex = data.length;
            askedForToomuch = true;
        }

        //If the given readBeginIndex is too high or there is nothing to send back.
        if (offset >= readEndIndex) {
            response.setStatusCode(StatusCode.ERROR_COMMAND_NOT_ALLOWED);
            return response;
        }
        response.setStatusCode(askedForToomuch ? StatusCode.WARNING_END_OF_FILE : StatusCode.SUCCESSFUL_PROCESSING);
        response.setData(Arrays.copyOfRange(data, offset, readEndIndex));
        return response;
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
}
