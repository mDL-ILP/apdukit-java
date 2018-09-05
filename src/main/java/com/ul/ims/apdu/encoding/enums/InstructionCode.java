package com.ul.ims.apdu.encoding.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Instruction code - indicates the specific command, e.g. "select file"
 *
 */
public enum InstructionCode {
    SELECT((byte)0xA4),
    READ_BINARY((byte)0xB0),
    INTERNAL_AUTHENTICATE((byte) 0x88);

    //Create a map connecting the byte values with the enum type.
    private static Map map = new HashMap<>();
    //Populate this map at the start.
    static {
        for (InstructionCode type : InstructionCode.values()) {
            map.put(type.value, type);
        }
    }
    //Returns a InstructionCode for a raw value byte.
    public static InstructionCode valueOf(byte type) {
        return (InstructionCode) map.get(type);
    }

    //Return byte value
    public byte getValue() {
        return value;
    }

    private final byte value;
    InstructionCode(byte i) {
        this.value = i;
    }
}
