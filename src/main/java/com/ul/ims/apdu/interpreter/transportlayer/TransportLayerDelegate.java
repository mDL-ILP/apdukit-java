package com.ul.ims.apdu.interpreter.transportlayer;

public interface TransportLayerDelegate {
    void onReceive(byte[] data);
    void onEvent(String string, int i);
}
