package com.ul.ims.apdu.interpreter.applicationLayer;

import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.interpreter.presentationLayer.PresentationLayer;
import com.ul.ims.apdu.interpreter.presentationLayer.PresentationLayerDelegate;

/**
 * Base class for a type of application.
 */
abstract class ApplicationLayer implements PresentationLayerDelegate {
    protected DedicatedFileID appId;
    protected PresentationLayer presentationLayer;

    public ApplicationLayer(PresentationLayer presentationLayer, DedicatedFileID appId) {
        this.appId = appId;
        this.presentationLayer = presentationLayer;
        presentationLayer.setDelegate(this);
    }

    @Override
    public DedicatedFileID getAppId() {
        return this.appId;
    }

    public void setAppId(DedicatedFileID appId) {
        this.appId = appId;
    }
}
