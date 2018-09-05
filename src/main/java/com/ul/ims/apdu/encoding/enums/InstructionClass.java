package com.ul.ims.apdu.encoding.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Instruction class - indicates the type of command, e.g. inter industry or proprietary
 *
 */
public enum InstructionClass {
    DEFAULT((byte)0x00),
    SECURE_MESSAGING((byte)0x0C);

    //Create a map connecting the byte values with the enum type.
    private static Map map = new HashMap<>();
    //Populate this map at the start.
    static {
        for (InstructionClass type : InstructionClass.values()) {
            map.put(type.value, type);
        }
    }
    //Returns a InstructionCode for a raw value byte.
    public static InstructionClass valueOf(byte type) {
        return (InstructionClass) map.get(type);
    }

    //Return byte value
    public byte getValue() {
        return value;
    }

    private final byte value;
    InstructionClass(byte i) {
        this.value = i;
    }
}
