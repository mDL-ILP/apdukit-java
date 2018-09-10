package com.ul.ims.apdu.interpreter.PresentationLayer;

import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.encoding.types.FileID;

public interface PresentationLayerDelegate {
    ApduFile getLocalFile(ElementaryFileID id);
    DedicatedFileID getAppId();
    boolean checkAccessConditions(ElementaryFileID file);
}
