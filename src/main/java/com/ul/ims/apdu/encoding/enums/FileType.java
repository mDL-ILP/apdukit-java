package com.ul.ims.apdu.encoding.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * SelectFileType - indicates what kind type of file it should expect.
 *
 */
public enum FileType {
    //Dedicated file
    DF((byte)0x04),
    //Elementary file
    EF((byte)0x02);

    private static Map map = new HashMap<>();
    static {
        for (FileType type : FileType.values()) {
            map.put(type.value, type);
        }
    }
    public static FileType valueOf(byte type) {
        return (FileType) map.get(type);
    }

    private final byte value;

    public byte getValue() {
        return value;
    }
    FileType(byte i) {
        this.value = i;
    }

}
