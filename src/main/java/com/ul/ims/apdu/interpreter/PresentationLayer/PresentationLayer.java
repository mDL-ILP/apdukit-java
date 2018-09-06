package com.ul.ims.apdu.interpreter.PresentationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduFileException;
import com.ul.ims.apdu.interpreter.SessionLayer.SessionLayerDelegate;

public interface PresentationLayer extends SessionLayerDelegate {

    Promise<byte[]> getFile(ElementaryFileID elementaryFileID);
    boolean setFile(ElementaryFileID id, byte[] data) throws InvalidApduFileException;
    void setDelegate(PresentationLayerDelegate delegate);

    void setMaximumExpectedLength(short value);
    int getMaximumExpectedLength();
}
