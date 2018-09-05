package com.ul.ims.apdu.interpreter.PresentationLayer;

import com.ul.ims.apdu.encoding.types.FileID;

public interface PresentationLayerDelegate {
    boolean checkAccessConditions(FileID id);
}
