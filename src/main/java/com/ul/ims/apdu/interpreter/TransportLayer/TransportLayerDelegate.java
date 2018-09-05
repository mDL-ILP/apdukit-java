package com.ul.ims.apdu.interpreter.TransportLayer;

import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;

import java.io.IOException;

public interface TransportLayerDelegate {
    void onReceive(byte[] data) throws IOException, InvalidApduException;
}
