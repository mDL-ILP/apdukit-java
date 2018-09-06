package com.ul.ims.apdu.encoding.utilities;

import com.ul.ims.apdu.encoding.Constants;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import org.junit.Test;
import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

//Normal means not extended length
public class ApduLengthUtilsUnitTests {

    @Test
    public void testEncodeDataLengthExtended() {
        short length = 261;//Value higher than 255
        byte[] result = ApduLengthUtils.encodeDataLength(length);
        assertEquals("Value is higher than 255. So length is represented in 3 bytes", 3, result.length);
        assertEquals("First byte should be 0 to indicate that its extended length", 0, result[0]);
        assertArrayEquals(new byte[]{(byte) 0x00, (byte) 0x01, (byte) 0x05}, result);
    }

    @Test
    public void testEncodeDataLengthNormal() {
        short length = 5;//Value lower than 255
        byte[] result = ApduLengthUtils.encodeDataLength(length);
        assertEquals("Value is lower than 255. So length is represented in 1 byte", 1, result.length);
        assertEquals("First byte should be the length given", 0x05, result[0]);
    }

    @Test
    public void testDecodeDataLengthNormal() throws Exception {
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(new byte[] {7, 0, 0, 0, 0, 0, 0, 0});
        assertEquals(7, ApduLengthUtils.decodeDataLength(stream));
    }

    @Test(expected = ParseException.class)
    public void testDecodeDataLengthNormalFailure() throws Exception {
        byte[] data = new byte[600];
        data[0] = 1;
        data[1] = 0x02;
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(data);
        ApduLengthUtils.decodeDataLength(stream);
    }

    @Test(expected = ParseException.class)
    public void testDecodeDataLength_InvalidData() throws InvalidNumericException, ParseException {
        ApduLengthUtils.decodeDataLength(new ByteArrayInputStreamExtension(new byte[]{}));
    }

    @Test
    public void testDecodeDataLengthExtendedSuccess() throws Exception {
        byte[] data = new byte[263];
        data[0] = 0;
        data[1] = 0x01;
        data[2] = 0x04;
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(data);
        assertEquals(260,  ApduLengthUtils.decodeDataLength(stream));
    }

    @Test(expected = ParseException.class)
    public void testDecodeDataLengthExtendedFailure() throws Exception {
        byte[] data = new byte[263];
        data[0] = 0;
        data[1] = 0x00;
        data[2] = (byte)0xFF;
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(data);
        assertEquals(260,  ApduLengthUtils.decodeDataLength(stream));
    }

    @Test
    public void testDecodeMaxExpectedLengthNormal() throws Exception {
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(new byte[] {5});
        assertEquals(5, ApduLengthUtils.decodeMaxExpectedLength(stream));
    }

    @Test
    public void testDecodeMaxExpectedLengthExtended() throws Exception {
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(new byte[] {0x00});
        assertEquals(256, ApduLengthUtils.decodeMaxExpectedLength(stream));
    }

    @Test
    public void testDecodeMaxExpectedLengthExtended2() throws Exception {
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(new byte[] {0x00, 0x00});
        assertEquals(Constants.DEFAULT_MAX_EXPECTED_LENGTH_EXTENDED, ApduLengthUtils.decodeMaxExpectedLength(stream));
    }

    @Test
    public void testDecodeMaxExpectedLengthExtended3() throws Exception {
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(new byte[] {0x00, 0x01, 0x02});
        assertEquals(258, ApduLengthUtils.decodeMaxExpectedLength(stream));
    }

    @Test(expected = ParseException.class)
    public void testDecodeMaxExpectedLengthExtendedFailure() throws Exception {
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(new byte[] {0x01, 0x05});
        ApduLengthUtils.decodeMaxExpectedLength(stream);
    }

    @Test(expected = ParseException.class)
    public void testDecodeMaxExpectedLengthExtendedFailure2() throws Exception {
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(new byte[] {0x00, 0x01});
        ApduLengthUtils.decodeMaxExpectedLength(stream);
    }

    @Test
    public void testEncodeMaxExpectedLengthExtended_EdgeCase() throws InvalidNumericException {
        byte[] value = ApduLengthUtils.encodeMaxExpectedLength(Constants.DEFAULT_MAX_EXPECTED_LENGTH_EXTENDED);
        assertArrayEquals(new  byte[]{0x00, 0x00}, value);
    }

    @Test(expected = InvalidNumericException.class)
    public void testInvalidMaxExpectedLengthEncoding() throws InvalidNumericException {
        ApduLengthUtils.encodeMaxExpectedLength(65537);
    }

}
