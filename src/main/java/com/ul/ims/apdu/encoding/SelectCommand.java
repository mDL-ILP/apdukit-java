package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.utilities.ConversionUtils;
import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.encoding.enums.InstructionCode;
import com.ul.ims.apdu.encoding.enums.ExpectedResultType;
import com.ul.ims.apdu.encoding.enums.SelectFileType;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.ValueNotSetException;
import com.ul.ims.apdu.encoding.types.FileID;
import com.ul.ims.apdu.encoding.utilities.ApduLengthUtils;
import com.ul.ims.apdu.extensions.OutOfBytesException;

public class SelectCommand extends CommandApdu {
    private SelectFileType fileType;
    private ExpectedResultType expectedResult;
    private FileID fileID;

    public SelectCommand() {
        super(InstructionCode.SELECT);
    }

    public SelectCommand(ByteArrayInputStreamExtension stream) throws Exception {
        super(stream);
        this.fileType = SelectFileType.valueOf(stream.readByte());
        this.expectedResult = ExpectedResultType.valueOf(stream.readByte());
        this.decodeFileId(stream);
        validate();
    }

    public SelectCommand setExpectedResult(ExpectedResultType expectedResult) {
        this.expectedResult = expectedResult;
        return this;
    }

    public SelectCommand setFileID(FileID id) {
        this.fileID = id;
        if(this.fileID instanceof ElementaryFileID) {
            this.fileType = SelectFileType.EF;
        }
        if(this.fileID instanceof DedicatedFileID) {
            this.fileType = SelectFileType.DF;
        }
        return this;
    }

    public SelectFileType getFileType() {
        return fileType;
    }

    public ExpectedResultType getExpectedResult() {
        return expectedResult;
    }

    public FileID getFileID() {
        return fileID;
    }

    @Override
    public void validate() throws InvalidApduException {
        super.validate();
        if(instructionCode != InstructionCode.SELECT) {
            throw new InvalidApduException("InstructionCode is not SELECT");
        }
        if(fileID == null) {
            throw new ValueNotSetException("fileID");
        }
        if(fileType == null) {
            throw new ValueNotSetException("fileType");
        }
        //Check that the filetype is correct.
        if(fileType == SelectFileType.EF && !(fileID instanceof ElementaryFileID) ) {
            throw new InvalidApduException("filetype does not match file id instance type");
        }
        if(fileType == SelectFileType.DF && !(fileID instanceof DedicatedFileID) ) {
            throw new InvalidApduException("filetype does not match file id instance type");
        }
        if(expectedResult == null) {
            throw new ValueNotSetException("expectedResult");
        }
    }

    //Called upon reading, parsing.
    private void decodeFileId(ByteArrayInputStreamExtension stream) throws ParseException {
        try {
            int fileIdLength = ApduLengthUtils.decodeDataLength(stream);
            byte[] fileId = stream.readBytes(fileIdLength);

            switch (fileType) {
                case EF:
                    this.fileID = new ElementaryFileID(null, ConversionUtils.fromBytesToShort(fileId));
                    break;
                case DF:
                    this.fileID = new DedicatedFileID(fileId);
            }
        }catch (OutOfBytesException e) {
            throw new ParseException("out of bytes");
        }
    }

    //Called when writing, encoding
    private void encodeFileId(ByteArrayOutputStream stream) throws InvalidApduException, IOException {
        byte[] fileId = this.fileID.getValue();
        if(fileId == null || fileId.length == 0) {
            throw new InvalidApduException("No file id set");
        }

        byte[] fileIdLength = ApduLengthUtils.encodeDataLength((short) fileId.length);
        stream.write(fileIdLength); //length
        stream.write(fileId); //Data
    }

    @Override
    public ByteArrayOutputStream toBytes() throws IOException, InvalidApduException, InvalidNumericException {
        this.validate();
        ByteArrayOutputStream stream = super.toBytes();
        stream.write(fileType.getValue()); //P1
        stream.write(expectedResult.getValue()); //P2
        encodeFileId(stream);
        return stream;
    }

}
