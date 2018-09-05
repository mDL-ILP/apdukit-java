package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.enums.FileControlInfo;
import com.ul.ims.apdu.encoding.enums.SelectFileType;
import com.ul.ims.apdu.apps.ExampleApp;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

//All the parse tests to go from an bytes to an object.
public class SelectCommandParseTests {

    @Test
    public void testSelectDFSuccess() throws Exception {
        byte[] input = {(byte) 0x0, (byte) 0xA4, (byte) 0x04, (byte) 0x0C,
                (byte) 0x07, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x02,
                (byte) 0x48, (byte) 0x04, (byte) 0x00};
        CommandApdu command = CommandApdu.fromBytes(input);
        assertTrue(command instanceof SelectCommand);
        SelectCommand result = (SelectCommand) command;
        assertEquals(SelectFileType.DF, result.getFileType());
        assertEquals(FileControlInfo.NOFCIReturn,result.getFileControlInfo());
        assertNotNull(result.getFileID());
        assertArrayEquals(ExampleApp.instance.ValidDF_NormalLength2.getValue(), result.getFileID().getValue());
    }

    @Test
    public void testSelectDFExtendedSuccess() throws Exception {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(new byte[]{(byte) 0x0, (byte) 0xA4, (byte) 0x04, (byte) 0x0C, (byte) 0x00, (byte) 0x01, (byte) 0x05});
            stream.write(ExampleApp.instance.ValidDF_ExtendedLength.getValue());

            CommandApdu command = CommandApdu.fromBytes(stream.toByteArray());
            assertTrue(command instanceof SelectCommand);
            SelectCommand result = (SelectCommand) command;
            assertEquals(SelectFileType.DF, result.getFileType());
            assertEquals(FileControlInfo.NOFCIReturn, result.getFileControlInfo());
            assertArrayEquals(ExampleApp.instance.ValidDF_ExtendedLength.getValue(), result.getFileID().getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSelectEFSuccess() throws Exception {
        byte[] data = {(byte) 0x00, (byte) 0xA4, (byte) 0x02, (byte) 0x0C, (byte) 0x02, (byte) 0x01, (byte) 0x1C};
        CommandApdu command = CommandApdu.fromBytes(data);
        assertTrue(command instanceof SelectCommand);
        SelectCommand result = (SelectCommand) command;
        assertEquals(SelectFileType.EF, result.getFileType());
        assertEquals(FileControlInfo.NOFCIReturn,result.getFileControlInfo());
        assertArrayEquals(ExampleApp.instance.ValidEF_NoShortId.getNormalIdentifier(), result.getFileID().getValue());
    }

}
