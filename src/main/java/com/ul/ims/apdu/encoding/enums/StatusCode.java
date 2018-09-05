package com.ul.ims.apdu.encoding.enums;

import com.ul.ims.apdu.encoding.utilities.ConversionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * StatusCode - indicates the status code of the response. Success, warning, error.
 *
 */
public enum StatusCode {
    //Indicates successful processing of the command APDU
    SUCCESSFUL_PROCESSING((short) 0x9000),
    //Indicates warning that the read APDU worked but that this is the end of the file
    WARNING_END_OF_FILE((short) 0x6282),
    //Indicates that the request had an incorrect length
    ERROR_INCORRECT_LENGTH((short) 0x6700),
    //Indicates that the request was denied
    ERROR_COMMAND_NOT_ALLOWED((short) 0x6986),
    //Indicates that the requested file was not found
    ERROR_FILE_NOT_FOUND((short) 0x6A82),
    //Indicates that the command APDU had invalid parameters
    ERROR_INVALID_PARAMETERS((short) 0x6A86),
    //Indicates that the command APDU didn't have the right parameters
    ERROR_WRONG_PARAMETERS((short) 0x6B00),
    //Indicates a generic error.
    ERROR_UNKNOWN((short) 0x6F00),
    //Indicates that the security status isn't high enough.
    ERROR_SECURITY_STATUS_NOT_SATISFIED((short) 0x6982);

    private static Map map = new HashMap<>();
    static {
        for (StatusCode type : StatusCode.values()) {
            map.put(type.value, type);
        }
    }
    public static StatusCode valueOf(short type) {
        return (StatusCode) map.get(type);
    }

    public byte[] getValue() {
        return ConversionUtils.fromShortToBytes(value);
    }

    public short getShortValue() {
        return value;
    }

    private final short value;
    StatusCode(short i) {
        this.value = i;
    }
}
