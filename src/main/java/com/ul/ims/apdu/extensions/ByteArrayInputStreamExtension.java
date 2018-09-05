package com.ul.ims.apdu.extensions;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * This class extends on ByteArrayInputStreamExtension and overrides the read method so that it throws an exception when its out of bytes instead of returning -1!
 * This is done to be consistent with the Swift version and so that a -1 byte doesn't get misinterpreted as a valid byte value: 0.
 */
public class ByteArrayInputStreamExtension extends ByteArrayInputStream {
    public ByteArrayInputStreamExtension(byte[] buf) {
        super(buf);
    }

    public ByteArrayInputStreamExtension(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    /**
     * Overriding read method and disabling it because function signature doesn't throw any exception.
     *
     * @deprecated use {@link #readByte()} instead.
     */
    @Override
    @Deprecated
    public synchronized int read() {
        throw new RuntimeException("Please use readByte method.");
    }

    /**
     * Overriding read method and disabling it because function signature doesn't throw any exception.
     *
     * @deprecated use {@link #readBytes(int size)} instead.
     */
    @Override
    @Deprecated()
    public int read(byte b[]) throws IOException {
        throw new RuntimeException("Please use readBytes method.");
    }

    /**
     * Reads the next byte of data from this input stream. If no byte is available
     * because the end of the stream has been reached, the function will return null.
     * <p>
     * This <code>readByte</code> method
     * cannot block.
     * @return  the next byte of data.
     */
    public Byte readByte() {
        int value = super.read();
        if(value == -1) {
            return null;
        }
        return (byte)value;
    }

    /**
     * Reads the next bytes of data from this input stream. The value
     * of byte array. The function will throw an exception if the callee
     * asked for too many bytes than that are available in the stream.
     * so it is wise to call available() first to find out how many bytes
     * can be asked.
     * @return  the next byte of data, or <code>-1</code> if the end of the
     *          stream has been reached.
     */
    public byte[] readBytes(int size) throws OutOfBytesException {
        byte[] result = new byte[size];
        for(int i = 0; i < size; i++) {
            Byte value = this.readByte();
            if(value == null) {
                throw new OutOfBytesException();
            }
            result[i] =  value;
        }
        return result;
    }
}
