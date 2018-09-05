package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.exceptions.InvalidElementaryFileId;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.ValueNotSetException;
import com.ul.ims.apdu.encoding.utilities.ConversionUtils;

import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ReadBinaryShortFileIDCommand extends ReadBinaryCommand {

    private ElementaryFileID elementaryFileID;
    private byte offset;

    public ReadBinaryShortFileIDCommand() {super();}

    ReadBinaryShortFileIDCommand(ByteArrayInputStreamExtension stream) throws Exception{
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

    private void encodeElementaryFileID(ByteArrayOutputStream stream) throws InvalidApduException {
        stream.write(ConversionUtils.replaceBit8with1(elementaryFileID.getShortIdentifier()));
    }

    @Override
    public void validate() throws InvalidApduException{
        super.validate();
        if(elementaryFileID == null) {
            throw new ValueNotSetException("elementaryFileID");
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

    public void setElementaryFileID(ElementaryFileID elementaryFileID) {
        this.elementaryFileID = elementaryFileID;
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
