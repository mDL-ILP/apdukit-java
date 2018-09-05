package com.ul.ims.apdu.encoding.types;

import java.util.Arrays;

public class DedicatedFileID implements FileID {
    private final byte[] value;

    public DedicatedFileID(byte[] value) {
        this.value = value;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if( !(obj instanceof DedicatedFileID) ) {
            return false;
        }
        DedicatedFileID other = (DedicatedFileID)obj;
        return Arrays.equals(this.value, other.value);
    }
}
