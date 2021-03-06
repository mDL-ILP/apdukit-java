package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.exceptions.*;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.encoding.utilities.ConversionUtils;

import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ReadBinaryShortFileIDCommand extends ReadBinaryCommand {

    private ElementaryFileID elementaryFileID;
    private Byte offset;

    public ReadBinaryShortFileIDCommand() {super();}

    ReadBinaryShortFileIDCommand(ByteArrayInputStreamExtension stream) throws ParseException {
        super(stream);
        this.decodeElementaryFileID(stream);
        this.decodeOffset(stream);
        this.decodeMaxExpectedLength(stream);
        validate();
    }

    private void decodeOffset(ByteArrayInputStreamExtension stream) {
        this.offset = stream.readByte();
    }

    private void encodeOffset(ByteArrayOutputStream stream) {
        stream.write(offset);
    }

    private void decodeElementaryFileID(ByteArrayInputStreamExtension stream) throws InvalidElementaryFileId {
        this.elementaryFileID = new ElementaryFileID(ConversionUtils.replaceBit8with0(stream.readByte()));
    }

    private void encodeElementaryFileID(ByteArrayOutputStream stream) {
        stream.write(ConversionUtils.replaceBit8with1(elementaryFileID.getShortIdentifier()));
    }

    @Override
    public void validate() throws InvalidApduException {
        super.validate();
        if(elementaryFileID == null) {
            throw new ValueNotSetException("elementaryFileID");
        }
        if(offset == null) {
            throw new ValueNotSetException("offset");
        }
    }

    @Override
    public ByteArrayOutputStream toBytes() throws IOException, InvalidApduException, InvalidNumericException {
        this.validate();
        ByteArrayOutputStream stream = super.toBytes();
        this.encodeElementaryFileID(stream);
        this.encodeOffset(stream);
        this.encodeMaxExpectedLength(stream);
        return stream;
    }

    public ReadBinaryShortFileIDCommand setElementaryFileID(ElementaryFileID elementaryFileID) {
        this.elementaryFileID = elementaryFileID;
        return this;
    }

    public ElementaryFileID getElementaryFileID() {
        return elementaryFileID;
    }

    public ReadBinaryShortFileIDCommand setOffset(byte offset) {
        this.offset = offset;
        return this;
    }

    public short getOffset() {
        return offset;
    }
}
