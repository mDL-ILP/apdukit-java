package com.ul.ims.apdu.interpreter.Mocks;

import com.ul.ims.apdu.apps.ExampleApp;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.ApplicationLayer.HolderApplicationLayer;
import com.ul.ims.apdu.interpreter.PresentationLayer.PresentationLayer;

public class TestHolder extends HolderApplicationLayer {
    public TestHolder(PresentationLayer presentationLayer) {
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
}