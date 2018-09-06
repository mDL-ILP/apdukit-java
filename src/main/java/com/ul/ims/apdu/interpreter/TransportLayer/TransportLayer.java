package com.ul.ims.apdu.interpreter.transportLayer;

import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;

import java.io.IOException;

public interface TransportLayer {
    void write(byte[] data) throws IOException, InvalidApduException;
    void close() throws IOException;
    void setDelegate(TransportLayerDelegate delegate);
}