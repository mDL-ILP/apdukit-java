package com.ul.ims.apdu.encoding.utilities;

import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class ConversionUtilsTests {

    @Test
    public void testBackAndForth8BitChanging() {
        byte from = (byte)0X1E;
        byte to = (byte)0x9E;
        assertEquals(from, ConversionUtils.replaceBit8with0(to));
        assertEquals(to, ConversionUtils.replaceBit8with1(from));
    }

    @Test
    public void testBytesToShort() throws InvalidNumericException {
        byte[] input = new byte[] {(byte) 0x01, (byte) 0x05};
        short expected = (short)261;

        Assert.assertEquals(expected, ConversionUtils.fromBytesToShort(input));
    }

    @Test
    public void testBytesToShort2() throws InvalidNumericException {
        byte[] input = new byte[] {(byte) 7};
        short expected = (short)7;

        Assert.assertEquals(expected, ConversionUtils.fromBytesToShort(input));
    }

    @Test
    public void testBytesToShort3() throws InvalidNumericException {
        byte[] input = new byte[] {(byte) 0x00, (byte) 0xff};
        short expected = (short)255;

        Assert.assertEquals(expected, ConversionUtils.fromBytesToShort(input));
    }

    @Test(expected = InvalidNumericException.class)
    public void testBytesToShort4() throws InvalidNumericException {
        byte[] input = new byte[] {(byte) 0x00, (byte) 0xff, (byte) 0xff};
        ConversionUtils.fromBytesToShort(input);
    }

    @Test
    public void testShortToBytes() {
        short value = (short) 0x9000;
        assertArrayEquals(new byte[]{(byte)0x90, (byte) 0x00}, ConversionUtils.fromShortToBytes(value));
    }

    @Test
    public void testByteToBits() {
        byte input = 1;
        byte[] result = ConversionUtils.byteToBits(input);
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}, result);

        input = 2;
        result = ConversionUtils.byteToBits(input);
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 1, 0}, result);
    }

    @Test
    public void testBytesToBits() {
        byte[] input = new byte[]{1};
        byte[] result = ConversionUtils.bytesToBits(input);
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 1}, result);

        input = new byte[]{1, 3};
        result = ConversionUtils.bytesToBits(input);
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1}, result);
    }

}
