package com.ul.ims.apdu.interpreter.presentationLayer;

import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;

public interface HolderPresentationLayerDelegate extends PresentationLayerDelegate {
    /**
     * Returns back a local file from this.files. Trying both short and normal.
     *
     * @param id elementaryFileID specifying the file
     * @return an ApduFile
     */
    ApduFile getLocalFile(ElementaryFileID id);
    /**
     * Delegate method that infroms the presentation layer if a request for a Elementary File is allowed
     * @param file that permission is asked for
     * @return boolean stating true if it is allowed or false if it's not.
     */
    boolean isFileAllowed(ElementaryFileID file);
    /**
     *  Delegate method that informs the presentation layer what the id is of this app.
     * @return the current dedicated file id
     */
    DedicatedFileID getAppId();
}
