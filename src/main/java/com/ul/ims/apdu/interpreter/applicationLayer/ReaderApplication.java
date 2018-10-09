package com.ul.ims.apdu.interpreter.applicationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.presentationLayer.PresentationLayer;
import com.ul.ims.apdu.interpreter.presentationLayer.PresentationLayerDelegate;
import com.ul.ims.apdu.interpreter.presentationLayer.ReaderPresentationLayer;

import java.util.concurrent.Semaphore;

/**
 * Is a type of application that reads files of that other application it is connected to.
 */
public abstract class ReaderApplication implements ReaderApplicationLayer {
    private DedicatedFileID appId;
    private ReaderPresentationLayer presentationLayer;
    //A lock so that we only get one file at a time.
    private Semaphore getFileLock = new Semaphore(1);

    public ReaderApplication(PresentationLayer presentationLayer, DedicatedFileID appId) {
        this.appId = appId;
        this.presentationLayer = (ReaderPresentationLayer) presentationLayer;
        this.presentationLayer.setDelegate(this);
    }

    /**
     * Gets an elementary file. Hangs if there is already an open request.
     *
     * @param id
     * @return
     */
    public Promise<byte[]> readFile(ElementaryFileID id) {
        try {
            getFileLock.acquire();
        } catch (InterruptedException e) {
            return Promise.reject(e);
        }
        return this.presentationLayer.selectDF(this.appId).then((res) -> {
            return openApduFile(id).then((file) -> this.resolveApduFile(file));
        }).always(() -> {
            getFileLock.release();
        });
    }

    /**
     * Creates the intial first part of a APDU file by selecting the ElementaryFileID on at the holder and read a few initial bytes.
     * This is done to know how large the file is. As all APDU files are TLV (tag, length, value). We'll know what to expect size wise.
     * Then the resolveApduFile method can download any remaining bytes left.
     * @param fileID the ElementaryFileID that needs to be opened.
     * @return Promise of a ApduFile
     */
    private Promise<ApduFile> openApduFile(ElementaryFileID fileID) {
        Promise<byte[]> firstChunk;
        if (fileID.isShortIDAvailable()) {
            firstChunk = this.presentationLayer.readBinary(fileID, (byte)0);//If short file id is available, a read will also instantly select the file.
        } else {
            firstChunk = this.presentationLayer.selectEF(fileID).then((v) -> this.presentationLayer.readBinary((byte)0));//Select and read the first part.
        }
        return firstChunk.then((data) -> {
            try {
                ApduFile result = new ApduFile(data);
                return Promise.resolve(result);
            }catch (Exception e) {
                return Promise.reject(e);
            }
        });
    }

    /**
     * This method will take a complete or incomplete APDu file and keeps reading until it is complete. Then return the bytes.
     * @param file APDU file. Created by reading the first 5 bytes or part of the file.\
     * @return
     */
    private Promise<byte[]> resolveApduFile(ApduFile file) {
        return new Promise<>(settlement -> {
            while (!file.isComplete()) {
                short offset = file.getCurrentSize();
                Promise<byte[]> promise = this.presentationLayer.readBinary(offset);
                try {
                    byte[] data = promise.getValue();
                    file.appendValue(data);
                } catch (Throwable e) {
                    settlement.reject(e);
                }
            }
            settlement.resolve(file.getData());
        });
    }

//    @Override
//    public DedicatedFileID getAppId() {
//        return this.appId;
//    }

    public void setAppId(DedicatedFileID id) {
        this.appId = id;
    }
}
