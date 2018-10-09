package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.apps.ExampleApp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReadBinaryShortFileIDCommandParseTests {

    @Test
    public void testReadBinaryOffsetCommandSimple() throws Exception {
        //Setup
        byte[] data = {0, (byte) 0xB0, (byte) 0x83, 40, 7};
        //Call
        CommandApdu command = CommandApdu.fromBytes(data);
        assertTrue(command instanceof ReadBinaryShortFileIDCommand);
        ReadBinaryShortFileIDCommand result = (ReadBinaryShortFileIDCommand) command;

        assertEquals(ExampleApp.instance.ValidEF2.getShortIdentifier(), result.getElementaryFileID().getShortIdentifier());
        assertEquals(40, result.getOffset());
        assertEquals(7,result.getMaximumExpectedLength());
    }

    @Test
    public void testReadBinaryOffsetCommandExtendedMaxExpectedLength() throws Exception {
        byte[] data = {0, (byte) 0xB0, (byte) 0x81, 40, (byte) 0x00, (byte) 0x01, (byte) 0x03};
        CommandApdu command = CommandApdu.fromBytes(data);
        assertTrue(command instanceof ReadBinaryShortFileIDCommand);
        ReadBinaryShortFileIDCommand result = (ReadBinaryShortFileIDCommand) command;

        assertEquals(ExampleApp.instance.ValidShortIdEF1.getShortIdentifier(),result.getElementaryFileID().getShortIdentifier());
        assertEquals(40, result.getOffset());
        assertEquals(259,result.getMaximumExpectedLength());
    }

}
