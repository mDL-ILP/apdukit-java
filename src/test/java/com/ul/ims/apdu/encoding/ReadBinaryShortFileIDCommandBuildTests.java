package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.exceptions.ValueNotSetException;
import com.ul.ims.apdu.apps.ExampleApp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ReadBinaryShortFileIDCommandBuildTests {

    ReadBinaryShortFileIDCommand subject;

    @Before
    public void runBeforeTestMethod() {
        subject = new ReadBinaryShortFileIDCommand();
    }

    @Test
    public void testOffsetBelow255() throws Exception {
        //Setup
        subject.setElementaryFileID(ExampleApp.instance.ValidEF2);
        subject.setOffset((byte) 40);
        subject.setMaximumExpectedLength((short) 7);

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertEquals("InstructionClass", 0, result[0]);
        assertEquals("InstructionCode", (byte)0xB0, result[1]);
        assertEquals("short file identifier", (byte) 0x83, result[2]);
        assertEquals("offset", (byte)0x28, result[3]);
        assertEquals("le", (byte)0x07, result[4]);
    }

    @Test
    public void testOffsetAbove255() throws Exception {
        //Setup
        subject.setElementaryFileID(ExampleApp.instance.ValidEF2);
        subject.setOffset((byte) 0x01);
        subject.setMaximumExpectedLength((short) 261);

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertEquals("InstructionClass", 0, result[0]);
        assertEquals("InstructionCode", (byte)0xB0, result[1]);
        assertEquals("short file identifier", (byte) 0x83, result[2]);
        assertEquals("offset", (byte)0x01, result[3]);
        assertEquals("le_1", (byte)0x0, result[4]);
        assertEquals("le_2", (byte)0x01, result[5]);
        assertEquals("le_3", (byte)0x05, result[6]);
    }

    @Test()
    public void testValidation() throws Exception {
        callValidationCheck("elementaryFileID");
        subject.setElementaryFileID(ExampleApp.instance.ValidShortIdEF1);
        subject.validate();
    }

    private void callValidationCheck(String value) {
        try {
            subject.validate();
            fail("It should've thrown an exception");
        }catch (ValueNotSetException e) {
            assertEquals(value, e.getValueName());
        } catch (Exception e) {
            fail("It threw the wrong exception");
        }
    }

}
