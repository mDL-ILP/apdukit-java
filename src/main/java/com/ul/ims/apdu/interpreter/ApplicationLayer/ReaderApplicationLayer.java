package com.ul.ims.apdu.interpreter.ApplicationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.PresentationLayer.PresentationLayer;

import java.util.concurrent.Semaphore;

public class ReaderApplicationLayer implements ApplicationLayer {

    private PresentationLayer presentationLayer;
    private DedicatedFileID appId;
    private Semaphore getFileLock = new Semaphore(1);

    public ReaderApplicationLayer(PresentationLayer presentationLayer, DedicatedFileID appId) {
        this.presentationLayer = presentationLayer;
        this.appId = appId;
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
            return this.presentationLayer.selectEF(elementaryFileID);
        }).always(() -> {
            getFileLock.release();
        });
    }

    @Override
    public ApduFile getLocalFile(ElementaryFileID id) {
        return null;
    }

    @Override
    public DedicatedFileID getAppId() {
        return this.appId;
    }

    @Override
    public boolean checkAccessConditions(ElementaryFileID file) {
        return false;
    }
}
