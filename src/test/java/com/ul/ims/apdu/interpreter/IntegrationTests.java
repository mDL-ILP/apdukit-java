package com.ul.ims.apdu.interpreter;

import com.ul.ims.apdu.interpreter.presentationLayer.*;
import com.ul.ims.apdu.interpreter.sessionLayer.*;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayerSimulator;
import org.junit.Before;

public class IntegrationTests {
    TransportLayerSimulator holderTransportLayer;
    TransportLayerSimulator readerTransportLayer;
    HolderSessionLayer holderSession;
    ReaderSessionLayer readerSession;
    HolderPresentationLayer holderPresentation;
    ReaderPresentationLayer readerPresentation;

    TestHolder holder;
    TestReader reader;

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

    void setupSessionLayers() {
        holderSession = new HolderSession(holderTransportLayer);
        readerSession = new ReaderSession(readerTransportLayer);
        setupPresentationLayers();
    }

    private void setupPresentationLayers() {
        holderPresentation = new HolderPresentation(holderSession);
        readerPresentation = new ReaderPresentation(readerSession);
        setupApplicationLayers();
    }

    private void setupApplicationLayers() {
        this.holder = new TestHolder(holderPresentation);
        this.reader = new TestReader(readerPresentation);
    }
}
