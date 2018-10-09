package com.ul.ims.apdu.interpreter.presentationLayer;

import com.ul.ims.apdu.interpreter.sessionLayer.SessionLayerDelegate;

public interface PresentationLayer extends SessionLayerDelegate {
    void setDelegate(PresentationLayerDelegate delegate);
}
