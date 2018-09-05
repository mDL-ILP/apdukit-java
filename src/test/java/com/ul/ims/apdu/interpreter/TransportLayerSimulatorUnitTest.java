package com.ul.ims.apdu.interpreter;

import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.interpreter.TransportLayer.TransportLayerSimulator;
import com.ul.ims.apdu.interpreter.TransportLayer.TransportLayerDelegate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketException;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TransportLayerSimulatorUnitTest {

    private TransportLayerSimulator internalTransportLayer;
    private TransportLayerSimulator externalTransportLayer;


    @Before
    public void runBefore() {
        this.internalTransportLayer = new TransportLayerSimulator();
        this.externalTransportLayer = mock(TransportLayerSimulator.class);
    }

    @Test
    public void writeOnInternalTransportLayer_CallsOnReceiveOnExternalTransportLayer() throws IOException, InvalidApduException {
        byte[] payload = new byte[5];
        internalTransportLayer.connect(externalTransportLayer);
        internalTransportLayer.write(payload);
        verify(externalTransportLayer,timeout(100).times(1)).onReceive(payload);
    }

    @Test(expected = SocketException.class)
    public void writeOnInternalTransportLayer_ThrowsErrorWhenNoConnectedExternalTransportLayer() throws IOException, InvalidApduException {
        internalTransportLayer.write(new byte[4]);
    }

    @Test
    public void onReceiveOnInternalTransportLayer_CallsOnReceiveOnDelegate() throws IOException, InvalidApduException {
        TransportLayerDelegate transportLayerDelegate = mock(TransportLayerDelegate.class);
        internalTransportLayer.setDelegate(transportLayerDelegate);
        byte[] payload = new byte[6];
        internalTransportLayer.onReceive(payload);
        verify(transportLayerDelegate,timeout(100).times(1)).onReceive(payload);
    }

    @Test(expected = SocketException.class)
    public void onReceiveOnInternalTransportLayer_ThrowsExceptionWhenNoDelegate() throws IOException, InvalidApduException {
        internalTransportLayer.onReceive(new byte[6]);
    }

    @Test
    public void callingClose_RevokesConnectionWithExternalTransportLayer() throws IOException, InvalidApduException {
        internalTransportLayer.connect(externalTransportLayer);
        internalTransportLayer.close();
        try {
            internalTransportLayer.write(new byte[7]);
        } catch (SocketException ex) {
            return;
        }
        Assert.fail("I had expected an exception!");
    }
}