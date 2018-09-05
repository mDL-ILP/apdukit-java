package com.ul.ims.apdu.encoding.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * SelectCommandExpected - indicates what kind of data we want returned in the ResponseApdu after a select command.
 *
 */
public enum ExpectedResultType {
    //NOFCIReturn: Return no file control info.
    NOTHING((byte)0x0C);

    public byte getValue() {
        return value;
    }

    private static Map map = new HashMap<>();
    static {
        for (ExpectedResultType type : ExpectedResultType.values()) {
            map.put(type.value, type);
        }
    }
    public static ExpectedResultType valueOf(byte type) {
        return (ExpectedResultType) map.get(type);
    }

    private final byte value;
    ExpectedResultType(byte i) {
        this.value = i;
    }

}
