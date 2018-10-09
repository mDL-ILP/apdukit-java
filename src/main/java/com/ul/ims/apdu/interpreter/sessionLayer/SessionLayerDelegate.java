package com.ul.ims.apdu.interpreter.sessionLayer;

import com.ul.ims.apdu.encoding.ReadBinaryCommand;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.SelectCommand;
import com.ul.ims.apdu.encoding.exceptions.ParseException;

public interface SessionLayerDelegate {
    void onSendFailure(Exception exception);//This can be a IO Exception (lost connection) or invalid APDU.
    // Informs the delegate when we've received an invalid apdu
    void onReceiveInvalidApdu(ParseException exception);
    // Informs the delegate upon receiving an event
    void onEvent(String string, int i);
}
