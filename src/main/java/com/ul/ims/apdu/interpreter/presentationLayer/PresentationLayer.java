package com.ul.ims.apdu.interpreter.presentationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.sessionLayer.SessionLayerDelegate;

public interface PresentationLayer extends SessionLayerDelegate {
    Promise selectDF(DedicatedFileID fileID);
    Promise selectEF(final ElementaryFileID fileID);
    Promise<byte[]> readEF(ElementaryFileID fileID);
    void setDelegate(PresentationLayerDelegate delegate);
}
