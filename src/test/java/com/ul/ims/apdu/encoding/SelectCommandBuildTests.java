package com.ul.ims.apdu.encoding;


import com.ul.ims.apdu.encoding.enums.FileControlInfo;
import org.junit.Before;
import org.junit.Test;

import com.ul.ims.apdu.encoding.exceptions.ValueNotSetException;
import com.ul.ims.apdu.apps.ExampleApp;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

//All the build tests to go from an object to bytes.
public class SelectCommandBuildTests {

    SelectCommand subject;

    @Before
    public void runBeforeTestMethod() {
        subject = new SelectCommand();
    }

    @Test
    public void testSelectDF() throws Exception {
        //Setup
        subject.setFileID(ExampleApp.instance.ValidDF_NormalLength2);
        subject.setFileControlInfo(FileControlInfo.NOFCIReturn);
        byte[] expected = new byte[]{(byte) 0x0, (byte) 0xA4, (byte) 0x04, (byte) 0x0C,
                (byte) 0x07, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x02,
                (byte) 0x48, (byte) 0x04, (byte) 0x00};

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertArrayEquals(expected, result);
    }

    @Test
    public void testSelectExtendedDF() throws Exception {
        //Setup
        subject.setFileID(ExampleApp.instance.ValidDF_ExtendedLength);
        subject.setFileControlInfo(FileControlInfo.NOFCIReturn);

        ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
        expectedStream.write(new byte[]{(byte) 0x0, (byte) 0xA4, (byte) 0x04, (byte) 0x0C, (byte) 0x00, (byte) 0x01, (byte) 0x05});
        expectedStream.write(ExampleApp.instance.ValidDF_ExtendedLength.getValue());

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertArrayEquals(expectedStream.toByteArray(), result);
    }

    @Test
    public void testSelectEF() throws Exception {
        //Setup
        subject.setFileID(ExampleApp.instance.ValidEF_NoShortId);
        subject.setFileControlInfo(FileControlInfo.NOFCIReturn);
        byte[] expected = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x02, (byte) 0x0C,
                (byte) 0x02, (byte) 0x01, (byte) 0x1C};

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertArrayEquals(expected, result);
    }

    @Test()
    public void testValidation() throws Exception {
        callValidation("fileID");
        subject.setFileID(ExampleApp.instance.ValidEF1);
        callValidation("fileControlInfo");
        subject.setFileControlInfo(FileControlInfo.NOFCIReturn);
        subject.validate();
    }

    private void callValidation(String value) {
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