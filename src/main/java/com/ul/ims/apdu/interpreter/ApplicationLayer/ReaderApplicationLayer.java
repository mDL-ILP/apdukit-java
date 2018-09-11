package com.ul.ims.apdu.interpreter.ApplicationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.PresentationLayer.PresentationLayer;

import java.util.concurrent.Semaphore;

/**
 * Is a type of application that reads files of that other application it is connected to.
 */
public abstract class ReaderApplicationLayer extends ApplicationLayer {
    //A lock so that we only get one file at a time.
    private Semaphore getFileLock = new Semaphore(1);

    public ReaderApplicationLayer(PresentationLayer presentationLayer, DedicatedFileID appId) {
        super(presentationLayer, appId);
    }

    /**
     * Gets an elementary file. Hangs if there is already an open request.
     *
     * @param elementaryFileID
     * @return
     */
    public Promise<byte[]> getFile(ElementaryFileID elementaryFileID) {
        try {
            getFileLock.acquire();
        } catch (InterruptedException e) {
            return Promise.reject(e);
        }
        return this.presentationLayer.selectDF(this.appId).then((res) -> {
            return this.presentationLayer.readEF(elementaryFileID);
        }).always(() -> {
            getFileLock.release();
        });
    }

    //Reader can only read.
    @Override
    public ApduFile getLocalFile(ElementaryFileID id) {
        return null;
    }

    //Reader doesn't implement check access conditions.
    @Override
    public boolean isFileAllowed(ElementaryFileID file) {
        return false;
    }
}
