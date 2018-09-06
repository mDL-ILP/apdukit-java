package com.ul.ims.apdu.interpreter.transportlayer;

import java.io.IOException;

public interface TransportLayerDelegate {
    void onReceive(byte[] data) throws IOException;
}
