package com.ul.ims.apdu.interpreter;

import com.ul.ims.apdu.interpreter.Mocks.TestHolder;
import com.ul.ims.apdu.interpreter.Mocks.TestReader;
import com.ul.ims.apdu.interpreter.PresentationLayer.ApduProtocolPresentationLayer;
import com.ul.ims.apdu.interpreter.PresentationLayer.PresentationLayer;
import com.ul.ims.apdu.interpreter.SessionLayer.ClientSessionLayer;
import com.ul.ims.apdu.interpreter.SessionLayer.ServerSessionLayer;
import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayer;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayerSimulator;
import org.junit.Before;
import org.junit.Test;

public class HolderIntegrationTests {

    private TransportLayerSimulator holderTransportLayer;
    private TransportLayerSimulator readerTransportLayer;
    private SessionLayer holderSessionLayer;
    private SessionLayer readerSessionLayer;
    private PresentationLayer holderPresentationLayer;
    private PresentationLayer readerPresentationLayer;

    private TestHolder holder;
    private TestReader reader;

    @Before
    public void setup() {
        setupTransportLayers();
    }

    private void setupTransportLayers() {
        holderTransportLayer = new TransportLayerSimulator();
        readerTransportLayer = new TransportLayerSimulator();
        holderTransportLayer.connect(readerTransportLayer);
        readerTransportLayer.connect(holderTransportLayer);
        setupSessionLayers();
    }

    private void setupSessionLayers() {
        holderSessionLayer = new ServerSessionLayer(holderTransportLayer);
        readerSessionLayer = new ClientSessionLayer(readerTransportLayer);
        setupPresentationLayers();
    }

    private void setupPresentationLayers() {
        holderPresentationLayer = new ApduProtocolPresentationLayer(holderSessionLayer);
        readerPresentationLayer = new ApduProtocolPresentationLayer(readerSessionLayer);
        setupApplicationLayers();
    }

    private void setupApplicationLayers() {
        this.holder = new TestHolder(holderPresentationLayer);
        this.reader = new TestReader(readerPresentationLayer);
    }

//    @Test
//    public void test_HolderOnReceive_UnknownError() throws Throwable {
//        this.transportLayerSimulatorHolder = mock(TransportLayerSimulator.class);
//        this.sessionLayerHolder = new ServerSessionLayer(transportLayerSimulatorHolder);
//        this.sessionLayerHolder.setDelegate(this.holder.holderPresentationLayer);
//
//        byte[] onReceiveData = new byte[] {(byte) 0x01, (byte) 0x02};
//        this.sessionLayerHolder.onReceive(onReceiveData);
//
//        verify(transportLayerSimulatorHolder, timeout(100).times(1)).write(new byte[]{(byte) 0x6F, (byte) 0x00});//Statuscode.ERROR_UNKNOWN
//    }

}
