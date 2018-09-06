package com.ul.ims.apdu.encoding.utilities;

import com.sun.tools.javac.util.ArrayUtils;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class ConversionUtils {
    /**
     * Given a short value, convert it to bytes.
     * @param value that needs to be converted
     * @return bytes that represent the given short value
     */
    public static byte[] fromShortToBytes(short value) {
        byte byte1 = (byte)((value >> Byte.SIZE) & 0xff);
        byte byte2 = (byte)(value & 0xff);
        return new byte[] {byte1,byte2};
    }

    /**
     * Given a bytes, convert it a short.
     * @param value that needs to be converted
     * @return short that represent the given byte value
     */
    public static short fromBytesToShort(byte[] value) throws InvalidNumericException {
        ByteBuffer wrapped = ByteBuffer.wrap(value);
        switch (value.length) {
            case 1:
                return value[0];
            case 2:
                return wrapped.getShort();
        }
        throw new InvalidNumericException("Couldn't convert byte to short");
    }


    /**
     * Converts bytes to bits
     * @param bytes bytes
     * @return array of bits for each byte. b.lenght * 8
     */
    public static byte[] bytesToBits(final byte[] bytes) {
        final byte[] bits = new byte[8 * bytes.length];
        int bitsIndex = 0;

        for (final byte _byte : bytes) {
            final byte[] bitsFromByte = toBitArray(_byte);
            System.arraycopy(bitsFromByte, 0, bits, bitsIndex, 8);
            bitsIndex +=8;
        }

        return bits;
    }

    /**
     * converts byte to an array of bits
     * @param bbyte byte
     * @return array of bits max 8 long.
     */
    public static byte[] toBitArray(final byte bbyte) {
        final byte[] bitArray = new byte[8];
        bitArray[7] = (byte) ((bbyte & 0x1) != 0 ? 1 : 0);
        bitArray[6] = (byte) ((bbyte & 0x2) != 0 ? 1 : 0);
        bitArray[5] = (byte) ((bbyte & 0x4) != 0 ? 1 : 0);
        bitArray[4] = (byte) ((bbyte & 0x8) != 0 ? 1 : 0);
        bitArray[3] = (byte) ((bbyte & 0x10) != 0 ? 1 : 0);
        bitArray[2] = (byte) ((bbyte & 0x20) != 0 ? 1 : 0);
        bitArray[1] = (byte) ((bbyte & 0x40) != 0 ? 1 : 0);
        bitArray[0] = (byte) ((bbyte & 0x80) != 0 ? 1 : 0);

        return  bitArray;
    }

    /**
     * converts byte to bits
     * @param b byte
     * @return array of bits max 8 long.
     */
    public static byte[] byteToBits(byte b) {
        final byte[] result = new byte[8];
        for (int i = 7; i > 0; i--) {
            if((b & 1) == 1) {
                result[i] = 1;
            } else {
                result[i] = 0;
            }
            b >>= 1;
        }
        return result;
    }

    /**
     * converts 8 bits to one byte
     * @param bits 8 bits.
     * @return The byte of the bits.
     */
    public static byte bitsToByte(byte[] bits) {
        if(bits.length != 8) {
            return 0;
        }
        byte result = 0;
        if (bits[7] == 1) result += 1;
        if (bits[6] == 1) result += 2;
        if (bits[5] == 1) result += 4;
        if (bits[4] == 1) result += 8;
        if (bits[3] == 1) result += 16;
        if (bits[2] == 1) result += 32;
        if (bits[1] == 1) result += 64;
        if (bits[0] == 1) result += 128;
        return result;
    }

    /**
     * replaces the 8th bit with a 0
     * @param value
     * @return replaced value
     */
    public static byte replaceBit8with0(byte value) {
        return  (byte) (value & ~(1 << 7));
    }

    /**
     * replaces the 8th bit with a 1
     * @param value that needs to be replaced
     * @return replaced value
     */
    public static byte replaceBit8with1(byte value) {
        return (byte) (value | (1 << 7));
    }

    /**
     * Returns true if the first three bits of the given byte are 0.0.0
     * @param value that needs to be checked
     * @return boolean returning if the statement is true
     */
    public static boolean areTheFirstThreeBitsZero(byte value)  {
        byte[] bits = byteToBits(value);
        return bits[0] == 0 && bits[1] == 0 && bits[2] == 0;
    }

    /**
     * Returns true if the first three bits of the given byte are a 1.0.0
     * @param value that needs to be checked
     * @return boolean returning if the statement is true
     */
    public static boolean areTheFirstThreeBits100(byte value) {
        byte[] bits = byteToBits(value);
        return bits[0] == 1 && bits[1] == 0 && bits[2] == 0;
    }
}
