package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.exceptions.ParseException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReadBinaryOffsetCommandParseTests {

    @Test
    public void testReadBinaryOffsetCommandSimple() throws Exception {
        byte[] data = {0, (byte) 0xB0, 0, 40, 7};
        CommandApdu command = CommandApdu.fromBytes(data);
        assertTrue(command instanceof ReadBinaryOffsetCommand);
        ReadBinaryOffsetCommand result = (ReadBinaryOffsetCommand) command;
        assertEquals(40, result.getOffset());
        assertEquals((short)7, result.getMaximumExpectedLength());
    }

    @Test
    public void testReadBinaryOffsetCommandExtendedLength() throws Exception {
        byte[] data = {0, (byte) 0xB0, (byte) 0x00, 33, (byte) 0x00, (byte) 0x01, (byte) 0x05};
        CommandApdu command = CommandApdu.fromBytes(data);
        assertTrue(command instanceof ReadBinaryOffsetCommand);
        ReadBinaryOffsetCommand result = (ReadBinaryOffsetCommand) command;
        assertEquals(33, result.getOffset());
        assertEquals((short)261, result.getMaximumExpectedLength());
    }

    @Test(expected = ParseException.class)
    public void testInvalidReadBinaryOffset() throws Exception {
        byte[] data = {0, (byte) 0xB0, (byte) 0x00, (byte) 0x00};
        CommandApdu.fromBytes(data);
    }

    @Test
    public void testZeroMeansExtendedLength() throws Exception {
        byte[] data = {0, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        CommandApdu command = CommandApdu.fromBytes(data);
        assertTrue(command instanceof ReadBinaryOffsetCommand);
        ReadBinaryOffsetCommand result = (ReadBinaryOffsetCommand) command;
        assertEquals(Constants.DEFAULT_MAX_EXPECTED_LENGTH_EXTENDED, result.getMaximumExpectedLength());
    }

}
