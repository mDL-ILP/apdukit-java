package com.ul.ims.apdu.interpreter.applicationLayer;

import com.onehilltech.promises.Promise;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduFileException;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.presentationLayer.PresentationLayer;

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
            return this.readEF(elementaryFileID);
        }).always(() -> {
            getFileLock.release();
        });
    }

    /**
     * Routes call to right EF read. If short it'll use the short id otherwise it'll use the normal.
     * @param fileID
     * @return
     */
    public Promise<byte[]> readEF(ElementaryFileID fileID) {
        return openApduFile(fileID).then((file) -> this.resolveApduFile(file));
    }

    /**
     * Creates the intial first part of a APDU file by selecting the ElementaryFileID on at the holder and read a few initial bytes.
     * @param fileID
     * @return
     */
    private Promise<ApduFile> openApduFile(ElementaryFileID fileID) {
        Promise<byte[]> promise;
        //If short file id is available, a read will also instantly select the file.
        if (fileID.isShortIDAvailable()) {
            promise = this.presentationLayer.readBinary(fileID, (byte)0);
        } else {
            promise = this.presentationLayer.selectEF(fileID).then((v) -> this.presentationLayer.readBinary((byte)0));//Select and read the first part.
        }
        return promise.then((data) -> {
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
        if (file == null) {
            return Promise.reject(new InvalidApduFileException("File is null"));
        }
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
