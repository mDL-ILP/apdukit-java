package com.ul.ims.apdu.interpreter;

import com.ul.ims.apdu.apps.ExampleApp;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.applicationLayer.HolderApplication;
import com.ul.ims.apdu.interpreter.presentationLayer.PresentationLayer;

public class TestHolder extends HolderApplication {
    TestHolder(PresentationLayer presentationLayer) {
        super(presentationLayer, ExampleApp.instance.ValidDF_NormalLength1);
    }

    @Override
    public boolean isFileAllowed(ElementaryFileID file) {
        return true;
    }

    @Override
    public void onSendFailure(Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public void onReceiveInvalidApdu(ParseException exception) {
        exception.printStackTrace();
    }

    /**
     * Informs the delegate upon receiving an event
     *
     * @param string
     * @param i
     */
    @Override
    public void onEvent(String string, int i) {

    }
}