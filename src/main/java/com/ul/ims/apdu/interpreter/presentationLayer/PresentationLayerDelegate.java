package com.ul.ims.apdu.interpreter.presentationLayer;

import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;

public interface PresentationLayerDelegate {
    /**
     * Returns back a local file from this.files. Trying both short and normal.
     *
     * @param id elementaryFileID specifying the file
     * @return an ApduFile
     */
    ApduFile getLocalFile(ElementaryFileID id);

    /**
     *  Delegate method that informs the presentation layer what the id is of this app.
     * @return the current dedicated file id
     */
    DedicatedFileID getAppId();

    /**
     * Delegate method that infroms the presentation layer if a request for a Elementary File is allowed
     * @param file that permission is asked for
     * @return boolean stating true if it is allowed or false if it's not.
     */
    boolean isFileAllowed(ElementaryFileID file);

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
}
