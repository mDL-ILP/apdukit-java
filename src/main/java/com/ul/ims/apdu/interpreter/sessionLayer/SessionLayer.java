package com.ul.ims.apdu.interpreter.sessionLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.CommandApdu;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.interpreter.transportlayer.TransportLayerDelegate;

/**
 * The session layer handles sending and receiving apdu messages. It decodes incoming bytes into Apdu objects and then calls the appropriate delegate message handle method.
 * It also allows for sending apdu commands and keeping track of this open request. Then fufilling the promise upon receiving data.
 */
public interface SessionLayer extends TransportLayerDelegate {
    void setDelegate(SessionLayerDelegate delegate);
}
