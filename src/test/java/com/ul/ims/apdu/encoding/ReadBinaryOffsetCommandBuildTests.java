package com.ul.ims.apdu.encoding;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ReadBinaryOffsetCommandBuildTests {

    ReadBinaryOffsetCommand subject;

    @Before
    public void runBeforeTestMethod() {
        subject = new ReadBinaryOffsetCommand();
    }

    @Test
    public void testOffsetBelow255() throws Exception {
        //Setup
        subject.setOffset((short)40);
        subject.setMaximumExpectedLength((short) 6);

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertEquals("InstructionClass", 0, result[0]);
        assertEquals("InstructionCode", -80, result[1]);
        assertEquals("most significant byte", 0, result[2]);
        assertEquals("least significant byte", 40, result[3]);
        assertEquals("le", 6, result[4]);
    }

    @Test
    public void testOffsetAbove255() throws Exception {
        //Setup
        subject.setOffset((short)270);//Above 255
        subject.setMaximumExpectedLength((short)123);

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertEquals("InstructionClass", 0, result[0]);
        assertEquals("InstructionCode", -80, result[1]);
        assertEquals("most significant byte", 0x01, result[2]);
        assertEquals("least significant byte", 0x0E, result[3]);
        assertEquals("le", 123, result[4]);
    }

    @Test
    public void testExtendedMaxExpLength() throws Exception {
        //Setup
        subject.setOffset((short)0x01);
        subject.setMaximumExpectedLength((short)261);

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertEquals("InstructionClass", 0, result[0]);
        assertEquals("InstructionCode", -80, result[1]);
        assertEquals("shortfileID", 0x00, result[2]);
        assertEquals("offset", 0x01, result[3]);

        assertEquals("length", 0x00, result[4]);
        assertEquals("length", 0x01, result[5]);
        assertEquals("length", 0x05, result[6]);
    }

}
