package com.ul.ims.apdu.interpreter.presentationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;

public interface ReaderPresentationLayer extends PresentationLayer {
    Promise selectDF(DedicatedFileID fileID);
    Promise selectEF(final ElementaryFileID fileID);
    Promise<byte[]> readBinary(ElementaryFileID fileID, byte offset);
    Promise<byte[]> readBinary(short offset);
}
