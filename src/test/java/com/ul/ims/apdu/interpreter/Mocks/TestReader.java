package com.ul.ims.apdu.interpreter.Mocks;

import com.ul.ims.apdu.apps.ExampleApp;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.interpreter.applicationLayer.ReaderApplicationLayer;
import com.ul.ims.apdu.interpreter.presentationLayer.PresentationLayer;

public class TestReader extends ReaderApplicationLayer {
    public TestReader(PresentationLayer presentationLayer) {
        super(presentationLayer, ExampleApp.instance.ValidDF_NormalLength1);
    }

    @Override
    public void onSendFailure(Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public void onReceiveInvalidApdu(ParseException exception) {
        exception.printStackTrace();
    }
}
