package com.ul.ims.apdu.interpreter.presentationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.sessionLayer.ReaderSessionLayerDelegate;

public interface ReaderPresentationLayer extends PresentationLayer, ReaderSessionLayerDelegate {
    Promise selectDF(DedicatedFileID fileID);
    Promise selectEF(final ElementaryFileID fileID);
    Promise<byte[]> readBinary(ElementaryFileID fileID, byte offset);
    Promise<byte[]> readBinary(short offset);
}
