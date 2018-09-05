package com.ul.ims.apdu.encoding.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * SelectFileType - indicates what kind type of file it should expect.
 *
 */
public enum SelectFileType {
    //Dedicated file
    DF((byte)0x04),
    //Elementary file
    EF((byte)0x02);

    private static Map map = new HashMap<>();
    static {
        for (SelectFileType type : SelectFileType.values()) {
            map.put(type.value, type);
        }
    }
    public static SelectFileType valueOf(byte type) {
        return (SelectFileType) map.get(type);
    }

    private final byte value;

    public byte getValue() {
        return value;
    }
    SelectFileType(byte i) {
        this.value = i;
    }

}
