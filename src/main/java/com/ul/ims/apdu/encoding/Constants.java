package com.ul.ims.apdu.encoding;

public class Constants {
    public static final int DEFAULT_MAX_EXPECTED_LENGTH_EXTENDED = 65536;
    public static final int TAG_BYTE_SIZE = 1;
    public static final int DEFAULT_MAX_EXPECTED_LENGTH_NOT_EXTENDED = 256;
    public static final int BYTE_OFFSET_TILL_LENGTH = Constants.TAG_BYTE_SIZE + 1;//At least the tag and then the one byte of length
    public static final int SIZE_RESPONSE_STATUS_CODE = 2;
    public static final byte DEFAULT_ALGORITHM_INFO = 0x00;
    public static final int DEFAULT_CHALLENGE_LENGTH = 8;
    public static final byte DEFAULT_REFERENCE_DATA_QUALIFIER = 0x00;
}
