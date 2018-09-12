package com.ul.ims.apdu.encoding.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * FileControlInfo - the file control information (FCI) is the string of data bytes available in response to a SELECT FILE command. #5.1.5 File control information
 * English: Indicates what kind of data we want returned in the ResponseApdu after a command
 */
public enum FileControlInfo {
    /// Return no file control info.
    NOFCIReturn((byte)0x0C);

    public byte getValue() {
        return value;
    }

    private static Map map = new HashMap<>();
    static {
        for (FileControlInfo type : FileControlInfo.values()) {
            map.put(type.value, type);
        }
    }
    public static FileControlInfo valueOf(byte type) {
        return (FileControlInfo) map.get(type);
    }

    private final byte value;
    FileControlInfo(byte i) {
        this.value = i;
    }

}
