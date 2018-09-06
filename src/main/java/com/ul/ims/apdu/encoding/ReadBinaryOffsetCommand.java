package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.utilities.ConversionUtils;
import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ReadBinaryOffsetCommand extends ReadBinaryCommand {

    private short offset = 0;

    public ReadBinaryOffsetCommand() {super();}

    public ReadBinaryOffsetCommand(ByteArrayInputStreamExtension stream) throws ParseException {
        super(stream);
        this.decodeOffset(stream);
        this.decodeMaxExpectedLength(stream);
        validate();
    }

    private void decodeOffset(ByteArrayInputStreamExtension stream) throws ParseException {
        try {
            byte[] offsetBuffer = stream.readBytes(Short.BYTES);
            this.offset = ConversionUtils.fromBytesToShort(offsetBuffer);
        } catch (Exception e) {
            throw new ParseException(e.getMessage());
        }
    }

    private void encodeOffset(ByteArrayOutputStream stream) throws IOException {
        byte[] offsetBuffer = ConversionUtils.fromShortToBytes(offset);
        stream.write(offsetBuffer);
    }

    @Override
    public ByteArrayOutputStream toBytes() throws IOException, InvalidApduException, InvalidNumericException {
        ByteArrayOutputStream stream = super.toBytes();
        this.encodeOffset(stream);
        this.encodeMaxExpectedLength(stream);
        return stream;
    }

    public ReadBinaryOffsetCommand setOffset(short offset) {
        this.offset = offset;
        return this;
    }

    public short getOffset() {
        return offset;
    }
}
