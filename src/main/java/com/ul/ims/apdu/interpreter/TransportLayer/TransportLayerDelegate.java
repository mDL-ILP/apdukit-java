package com.ul.ims.apdu.interpreter.transportLayer;

import java.io.IOException;

public interface TransportLayerDelegate {
    void onReceive(byte[] data) throws IOException;
}
