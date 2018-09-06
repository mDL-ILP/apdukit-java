package com.ul.ims.apdu.interpreter.SessionLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayerDelegate;

public interface SessionLayer extends TransportLayerDelegate {
    Promise<ResponseApdu> send(byte[] data);
    void setDelegate(SessionLayerDelegate delegate);
}
