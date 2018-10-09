package com.ul.ims.apdu.interpreter;

import com.ul.ims.apdu.interpreter.presentationLayer.*;
import com.ul.ims.apdu.interpreter.sessionLayer.*;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayerSimulator;
import org.junit.Before;

public class IntegrationTests {
    public TransportLayerSimulator holderTransportLayer;
    public TransportLayerSimulator readerTransportLayer;
    public HolderSessionLayer holderSession;
    public ReaderSessionLayer readerSession;
    public HolderPresentationLayer holderPresentation;
    public ReaderPresentationLayer readerPresentation;

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
        holderSession = new HolderSession(holderTransportLayer);
        readerSession = new ReaderSession(readerTransportLayer);
        setupPresentationLayers();
    }

    public void setupPresentationLayers() {
        holderPresentation = new HolderPresentation(holderSession);
        readerPresentation = new ReaderPresentation(readerSession);
        setupApplicationLayers();
    }

    public void setupApplicationLayers() {
        this.holder = new TestHolder(holderPresentation);
        this.reader = new TestReader(readerPresentation);
    }
}
