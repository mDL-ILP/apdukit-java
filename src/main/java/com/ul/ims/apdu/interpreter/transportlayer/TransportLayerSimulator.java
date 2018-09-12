package com.ul.ims.apdu.interpreter.transportlayer;

import java.io.IOException;
import java.net.SocketException;

public class TransportLayerSimulator implements TransportLayer {
    private TransportLayerDelegate delegate;
    private TransportLayerSimulator externalMockTransportLayer = null;

    public void connect(TransportLayerSimulator transportLayer) {
        externalMockTransportLayer = transportLayer;
    }

    @Override
    public void write(byte[] data) throws IOException {
        if (externalMockTransportLayer == null) {
            throw new SocketException();
        }
       externalMockTransportLayer.onReceive(data);
    }

    @Override
    public void close() {
        externalMockTransportLayer = null;
    }

    //Simulate a onReceive
    public void onReceive(byte[] data) throws IOException {
        if(this.delegate == null) {
            throw new SocketException();
        }
        this.delegate.onReceive(data);
    }

    @Override
    public void setDelegate(TransportLayerDelegate delegate) {
        this.delegate = delegate;
    }

}
