package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.enums.InstructionCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;

import com.ul.ims.apdu.encoding.utilities.ApduLengthUtils;
import com.ul.ims.apdu.encoding.utilities.ConversionUtils;
import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class ReadBinaryCommand extends CommandApdu {

    short maximumExpectedLength = 0;//0 means EXTENDED_LENGTH.

    ReadBinaryCommand() {
        super(InstructionCode.READ_BINARY);
    }

    ReadBinaryCommand(ByteArrayInputStreamExtension stream) throws ParseException {
        super(stream);
    }

    @Override
    public void validate() throws InvalidApduException {
        super.validate();
        if(instructionCode != InstructionCode.READ_BINARY) {
            throw new InvalidApduException("InstructionCode is not READ_BINARY");
        }
    }

    protected void decodeMaxExpectedLength(ByteArrayInputStreamExtension stream) throws ParseException {
        try {
            this.maximumExpectedLength = ApduLengthUtils.decodeMaxExpectedLength(stream);
        } catch (InvalidNumericException e) {
            throw new ParseException(e.getMessage());
        }
    }

    protected void encodeMaxExpectedLength(ByteArrayOutputStream stream) throws IOException {
        stream.write(ApduLengthUtils.encodeMaxExpectedLength(this.maximumExpectedLength));
    }

    static ReadBinaryCommand fromBytes(ByteArrayInputStreamExtension stream) throws ParseException {
        stream.skip(2);//Skip the instruction class + instruction code
        byte fileIdFirstByte = stream.readByte();
        stream.reset();
        if (ConversionUtils.areTheFirstThreeBits100(fileIdFirstByte)) {
            return new ReadBinaryShortFileIDCommand(stream);
        }
        return new ReadBinaryOffsetCommand(stream);
    }

    public void setMaximumExpectedLength(short size) {
        this.maximumExpectedLength = size;
    }

    public short getMaximumExpectedLength() {
        return this.maximumExpectedLength;
    }

    public abstract short getOffset();
}
