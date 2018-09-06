package com.ul.ims.apdu.interpreter.SessionLayer;

import com.ul.ims.apdu.encoding.ReadBinaryCommand;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.SelectCommand;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;

public interface SessionLayerDelegate {

    //Informs the delegate when we've received an invalid apdu
    void onReceiveInvalidApdu(ParseException exception);
    //Informs the delegate when got an exception when sending has failed.
    void onSendFailure(Exception exception);//This can be a IO Exception (lost connection) or invalid APDU.
}
