package com.ul.ims.apdu.interpreter.presentationLayer;

import com.ul.ims.apdu.encoding.exceptions.ParseException;

public interface PresentationLayerDelegate {
    /**
     *  Informs the delegate when got an exception when sending has failed.
     * @param exception
     */
    void onSendFailure(Exception exception);//This can be a IO Exception (lost connection) or invalid APDU.

    /**
     * Informs the delegate when we've received an invalid apdu
     * @param exception
     */
    void onReceiveInvalidApdu(ParseException exception);

    /**
     * Informs the delegate upon receiving an event
     */
    void onEvent(String string, int i);
}
