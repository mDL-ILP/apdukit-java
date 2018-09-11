package com.ul.ims.apdu.interpreter.ApplicationLayer;

import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.types.ApduFile;
import com.ul.ims.apdu.encoding.types.DedicatedFileID;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import com.ul.ims.apdu.interpreter.PresentationLayer.PresentationLayer;

import java.util.HashMap;

/**
 * A holder is a type of application that holds data (files).
 */
public abstract class HolderApplicationLayer extends ApplicationLayer {
    //State
    private HashMap<Short, ApduFile> localFiles = new HashMap<>();

    public HolderApplicationLayer(PresentationLayer presentationLayer, DedicatedFileID appId) {
        super(presentationLayer, appId);
    }

    /**
     * Sets the file for a particular file id on the holders side.
     *
     * @param id   the file id of the data
     * @param data bytes of the file.
     * @return boolean informing the caller if the file was successfully set.
     */
    public boolean setLocalFile(ElementaryFileID id, byte[] data) {
        ApduFile file;
        try {
            file = new ApduFile(data);
            if (!file.isComplete()) {
                return false;
            }
            localFiles.put(id.getNormalIDValueAsAShort(), file);
        } catch (Exception e) {
            return false;
        }

        try {
            localFiles.put(id.getShortIDValueAsAShort(), file);
        } catch (InvalidNumericException e) {
        }
        return true;
    }

    /**
     * Returns back a local file from this.files. Trying both short and normal.
     *
     * @param id elementaryFileID specifying the file
     * @return an ApduFile
     */
    public ApduFile getLocalFile(ElementaryFileID id) {
        try {
            short key = id.getShortIDValueAsAShort();
            if (localFiles.containsKey(key)) {
                return localFiles.get(key);
            }
        } catch (Exception ignored) {
        }
        try {
            short key = id.getNormalIDValueAsAShort();
            if (localFiles.containsKey(key)) {
                return localFiles.get(key);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

}
