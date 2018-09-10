package com.ul.ims.apdu.interpreter.SessionLayer;

import com.ul.ims.apdu.encoding.ReadBinaryCommand;
import com.ul.ims.apdu.encoding.ResponseApdu;
import com.ul.ims.apdu.encoding.SelectCommand;
import com.ul.ims.apdu.encoding.exceptions.ParseException;

public interface SessionLayerDelegate {
    //Responds with the binary data of that EF file.
    ResponseApdu receivedReadRequest(ReadBinaryCommand command);
    //Responds with the appropriate status code.
    ResponseApdu receivedSelectRequest(SelectCommand command);
    //Informs the delegate when got an exception when sending has failed.
    void onSendFailure(Exception exception);//This can be a IO Exception (lost connection) or invalid APDU.
    //Informs the delegate when we've received an invalid apdu
    void onReceiveInvalidApdu(ParseException exception);
}
