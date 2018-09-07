package com.ul.ims.apdu.interpreter.PresentationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.types.FileID;

public interface HolderPresentationLayerDelegate extends PresentationLayerDelegate {
    boolean checkAccessConditions(FileID id);
    Promise<byte[]> signChallenge(byte[] challenge);
}
