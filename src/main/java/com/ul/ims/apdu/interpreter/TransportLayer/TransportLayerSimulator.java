package com.ul.ims.apdu.interpreter.TransportLayer;

import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import java.io.IOException;
import java.net.SocketException;

public class TransportLayerSimulator implements TransportLayer {
    private TransportLayerDelegate delegate;
    private TransportLayer externalMockTransportLayer = null;

    public void connect(TransportLayer transportLayer) {
        externalMockTransportLayer = transportLayer;
    }

    @Override
    public void write(byte[] data) throws IOException, InvalidApduException {
        if (externalMockTransportLayer == null) {
            throw new SocketException();
        }
       externalMockTransportLayer.onReceive(data);
    }

    @Override
    public void close() {
        externalMockTransportLayer = null;
    }

    @Override
    public void onReceive(byte[] data) throws IOException, InvalidApduException {
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
