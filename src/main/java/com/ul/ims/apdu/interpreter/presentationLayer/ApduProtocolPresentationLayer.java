package com.ul.ims.apdu.interpreter.presentationLayer;

import com.ul.ims.apdu.encoding.ReadBinaryCommand;
import com.ul.ims.apdu.encoding.ReadBinaryShortFileIDCommand;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.SelectCommand;
import com.ul.ims.apdu.encoding.enums.SelectFileType;
import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.encoding.types.FileID;
import com.ul.ims.apdu.interpreter.sessionLayer.SessionLayer;

import java.util.Arrays;

/**
 *  The handle apdu protocol presentation layer. Extends the base apdu protocol layer with methods to know of to handle requests
 */
public class ApduProtocolPresentationLayer extends BaseApduProtocolPresentationLayer {

    public ApduProtocolPresentationLayer(SessionLayer sessionLayer) {
        super(sessionLayer);
    }

    @Override
    public ResponseApdu receivedSelectCommand(SelectCommand command) {
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
    public ResponseApdu receivedReadCommand(ReadBinaryCommand command) {
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
        ApduFile file = this.delegate.getLocalFile(id);
        if (file == null) {
            response.setStatusCode(StatusCode.ERROR_FILE_NOT_FOUND);
            return response;
        }
        //Check access
        if (delegate != null && !delegate.isFileAllowed(id)) {
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
     * Sets the selected app id (DedicatedFileID)and returns if this was successful.
     *
     * @param id dedicatedFileID specifying the app id
     * @return boolean indicating if the set was successful.
     */
    private boolean setSelectedApp(DedicatedFileID id) {
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
}
