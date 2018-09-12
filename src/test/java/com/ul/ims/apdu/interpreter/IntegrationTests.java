package com.ul.ims.apdu.interpreter;

import com.ul.ims.apdu.interpreter.Mocks.TestHolder;
import com.ul.ims.apdu.interpreter.Mocks.TestReader;
import com.ul.ims.apdu.interpreter.presentationLayer.ApduProtocolPresentationLayer;
import com.ul.ims.apdu.interpreter.presentationLayer.PresentationLayer;
import com.ul.ims.apdu.interpreter.sessionLayer.ClientSessionLayer;
import com.ul.ims.apdu.interpreter.sessionLayer.ServerSessionLayer;
import com.ul.ims.apdu.interpreter.sessionLayer.SessionLayer;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayerSimulator;
import org.junit.Before;

public class IntegrationTests {
    public TransportLayerSimulator holderTransportLayer;
    public TransportLayerSimulator readerTransportLayer;
    public SessionLayer holderSessionLayer;
    public SessionLayer readerSessionLayer;
    public PresentationLayer holderPresentationLayer;
    public PresentationLayer readerPresentationLayer;

    public TestHolder holder;
    public TestReader reader;

    @Before
    public void setup() {
        setupTransportLayers();
    }

    public void setupTransportLayers() {
        holderTransportLayer = new TransportLayerSimulator();
        readerTransportLayer = new TransportLayerSimulator();
        holderTransportLayer.connect(readerTransportLayer);
        readerTransportLayer.connect(holderTransportLayer);
        setupSessionLayers();
    }

    public void setupSessionLayers() {
        holderSessionLayer = new ServerSessionLayer(holderTransportLayer);
        readerSessionLayer = new ClientSessionLayer(readerTransportLayer);
        setupPresentationLayers();
    }

    public void setupPresentationLayers() {
        holderPresentationLayer = new ApduProtocolPresentationLayer(holderSessionLayer);
        readerPresentationLayer = new ApduProtocolPresentationLayer(readerSessionLayer);
        setupApplicationLayers();
    }

    public void setupApplicationLayers() {
        this.holder = new TestHolder(holderPresentationLayer);
        this.reader = new TestReader(readerPresentationLayer);
    }
}
