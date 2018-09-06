package com.ul.ims.apdu.interpreter;

import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.interpreter.transportLayer.TransportLayerSimulator;
import com.ul.ims.apdu.interpreter.transportLayer.TransportLayerDelegate;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketException;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TransportLayerSimulatorTests {

    private TransportLayerSimulator subject1;
    private TransportLayerSimulator subject2;//Is mocked

    @Before
    public void runBefore() {
        this.subject1 = new TransportLayerSimulator();
        this.subject2 = mock(TransportLayerSimulator.class);
    }

    @Test
    public void testWrite() throws IOException, InvalidApduException {
        byte[] payload = new byte[]{0, 1, 2, 3};
        subject1.connect(subject2);
        subject1.write(payload);
        verify(subject2, timeout(100).times(1)).onReceive(payload);
    }

    @Test(expected = SocketException.class)
    public void testDisconnectedWrite() throws IOException, InvalidApduException {
        subject1.write(new byte[4]);
    }

    @Test
    public void testOnReceive() throws IOException, InvalidApduException {
        TransportLayerDelegate transportLayerDelegate = mock(TransportLayerDelegate.class);
        subject1.setDelegate(transportLayerDelegate);
        byte[] payload = new byte[6];
        subject1.onReceive(payload);
        verify(transportLayerDelegate,timeout(100).times(1)).onReceive(payload);
    }

    @Test(expected = SocketException.class)
    public void testDisconnectedOnReceive() throws IOException, InvalidApduException {
        subject1.onReceive(new byte[6]);
    }

    @Test(expected = SocketException.class)
    public void testClose() throws IOException, InvalidApduException {
        subject1.connect(subject2);
        subject1.close();
        subject1.write(new byte[7]);
    }
}