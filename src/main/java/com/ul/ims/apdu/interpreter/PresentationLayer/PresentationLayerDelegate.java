package com.ul.ims.apdu.interpreter.PresentationLayer;

import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;

public interface PresentationLayerDelegate {
    ApduFile getLocalFile(ElementaryFileID id);
    DedicatedFileID getAppId();
    boolean checkAccessConditions(ElementaryFileID file);

    //Informs the delegate when got an exception when sending has failed.
    void onSendFailure(Exception exception);//This can be a IO Exception (lost connection) or invalid APDU.
    //Informs the delegate when we've received an invalid apdu
    void onReceiveInvalidApdu(ParseException exception);
}
