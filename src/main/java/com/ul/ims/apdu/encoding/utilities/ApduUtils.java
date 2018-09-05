package com.ul.ims.apdu.encoding.utilities;

import com.ul.ims.apdu.encoding.Constants;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;

import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Arrays;

//All the APDU cheat codes on planet earth.
public class ApduUtils {
    public static class TLVInfo {
        //Tag
        int tag;
        //Length of the TLV structure
        short length;
        //At what offset the actual data (value) starts
        int dataOffset;

        private TLVInfo(int tag, short length, int dataOffset) {
            this.tag = tag;
            this.length = length;
            this.dataOffset = dataOffset;
        }

        public int getTag() {
            return tag;
        }

        public short getLength() {
            return length;
        }

        public int getDataOffset() {
            return dataOffset;
        }

    }

    /**
     * Parses a TLV structure and returns the tag, length and dataOffset
     * @param data TLV data
     * @return TLVInfo containing TLV information
     */
    public static TLVInfo parseTLV(byte[] data) {
        if(data == null || data.length < Constants.BYTE_OFFSET_TILL_LENGTH) {
            return null;
        }
        int dataOffset = Constants.BYTE_OFFSET_TILL_LENGTH;
        int tag = data[0];
        byte[] lengthBytes = new byte[] {data[1]};

        //Check if the length value is exceeds 255
        byte[] bits = ConversionUtils.byteToBits(data[1]);
        //If the first bit is 1, this tells us that the value is higher than 255.
        //If its 1, just read the byte.
        if (bits[0] == 1) {
            bits[0] = 0;//Skip the firt bit. Then read the bits after.
            int lengthValueSize = ConversionUtils.bitsToByte(bits);
            int lengthEndOffset = Constants.BYTE_OFFSET_TILL_LENGTH + lengthValueSize;
            if(data.length < lengthEndOffset) {
                return null;
            }
            lengthBytes = Arrays.copyOfRange(data, Constants.BYTE_OFFSET_TILL_LENGTH, lengthEndOffset);
            dataOffset = lengthEndOffset;
        }
        try {
            short length = ConversionUtils.fromBytesToShort(lengthBytes);
            return new TLVInfo(tag, length, dataOffset);
        } catch (Exception ignored) {}
        return null;
    }
}
