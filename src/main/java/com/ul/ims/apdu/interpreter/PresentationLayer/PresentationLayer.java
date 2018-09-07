package com.ul.ims.apdu.interpreter.PresentationLayer;

import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayerDelegate;

public interface PresentationLayer extends SessionLayerDelegate {

    void setDelegate(PresentationLayerDelegate delegate);

//    void setMaximumExpectedLength(int value);
//    int getMaximumExpectedLength();
}
