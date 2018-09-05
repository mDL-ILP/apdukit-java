package com.ul.ims.apdu.extensions;

public class OutOfBytesException extends Exception {
    public OutOfBytesException() {
        super("Not enough bytes");
    }
}
