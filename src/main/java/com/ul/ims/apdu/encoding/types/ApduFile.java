package com.ul.ims.apdu.encoding.types;

import com.ul.ims.apdu.encoding.exceptions.InvalidApduFileException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ApduFile {
    private TLVInfo info;
    private short declaredSize;
    private ByteArrayOutputStream data;

    public ApduFile(byte[] data) throws ParseException {
        if(data.length < 2) {
            throw new InvalidApduFileException("Needs at least one byte for the tag. Another for the minimal length size which can be one byte.");
        }
        this.info = new TLVInfo(data);
        computeDeclaredSize();

        this.data = new ByteArrayOutputStream(this.declaredSize);
        try {
            this.data.write(data);
        }catch (Exception e) {
            throw new InvalidApduFileException(e.getMessage());
        }
        if(getCurrentSize() > getDeclaredSize()) {
            throw new InvalidApduFileException("Invalid TLV. The current size is larger than the declared size.");
        }
        if(remainingBytes() < 0) {
            throw new InvalidApduFileException("Invalid TLV. Remaining bytes is negative.");
        }
    }

    public byte[] getData() {
        if(!isComplete()) {
            return null;
        }
        return data.toByteArray();
    }

    public void appendValue(byte[] value) throws IOException {
        if(value.length > remainingBytes()) {
            throw new IOException("Actual data exceeds expected data");
        }
        this.data.write(value);
    }

    public boolean isComplete() {
        return getCurrentSize() >= getDeclaredSize();
    }

    public byte getTag() {
        return this.data.toByteArray()[0];
    }

    private void computeDeclaredSize() throws InvalidApduFileException {
        this.declaredSize = (short) ((this.info.getLength() + this.info.getDataOffset()));
        if(this.declaredSize <= 0) {
            throw new InvalidApduFileException("Total size is 0");
        }
    }

    public short getDeclaredSize() {
        return (declaredSize);
    }

    public short getCurrentSize() {
        return (short) data.size();
    }

    public short remainingBytes() {
        if(isComplete()) {
            return 0;
        }
        return (short) (getDeclaredSize() - getCurrentSize());
    }


    /*
    TODO: Make a layer check if the response is actually what it asked for.
    Also validate response status code.

    public void validateResponseDataTag(byte[] data, byte expectedTag) throws InvalidResponseTagException {
        if (data == null) {
            throw new InvalidResponseTagException ("Empty data");
        }
    if (data[0] != expectedTag) {
        throw new InvalidResponseTagException("Incorrect Data Tag");
    }
    }
    */

}
