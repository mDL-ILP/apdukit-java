package com.ul.ims.apdu.encoding.types;

import com.ul.ims.apdu.encoding.Constants;
import com.ul.ims.apdu.encoding.exceptions.ParseException;

import com.ul.ims.apdu.encoding.utilities.ConversionUtils;
import java.util.Arrays;

public class TLVInfo {
    /// Tag
    int tag;
    /// Length of the TLV structure
    short length;
    /// At what offset the actual data (value) starts
    int dataOffset;

    /**
     * Parses a TLV structure and returns the tag, length and dataOffset
     * @param data TLV data
     * @return TLVInfo containing TLV information
     */
    public TLVInfo(byte[] data) throws ParseException {
        if(data == null || data.length < Constants.BYTE_OFFSET_TILL_LENGTH) {
            throw new ParseException("Not enough bytes to parse TLV structure");
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
                throw new ParseException("Invalid TLV structure. Length doesn't make any sense");
            }
            lengthBytes = Arrays.copyOfRange(data, Constants.BYTE_OFFSET_TILL_LENGTH, lengthEndOffset);
            dataOffset = lengthEndOffset;
        }
        short length = ConversionUtils.fromBytesToShort(lengthBytes);
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