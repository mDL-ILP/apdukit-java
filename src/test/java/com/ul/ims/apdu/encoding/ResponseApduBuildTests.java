package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.enums.StatusCode;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertArrayEquals;

public class ResponseApduBuildTests {
    private ResponseApdu subject;

    @Before
    public void runBeforeTestMethod() {
        subject = new ResponseApdu();
    }

    @Test
    public void testSuccessfulProcessing() throws Exception{
        //Setup
        byte[] expected = new byte[] {(byte) 0x90, (byte) 0x00};
        subject.setStatusCode(StatusCode.SUCCESSFUL_PROCESSING);

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertArrayEquals(expected, result);
    }

    //Scenario: response to  (READ BINARY command: '00 B0 81 00 06')
    @Test
    public void testSuccessfulProcessingWithData() throws Exception{
        //Setup
        byte[] data = new byte[] {(byte) 0x71, (byte) 0x82, (byte) 0x01, (byte) 0x05, (byte) 0x80, (byte) 0x01};

        ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
        expectedStream.write(data);
        expectedStream.write(new byte[]{(byte) 0x90, (byte) 0x00});

        subject.setData(data);
        subject.setStatusCode(StatusCode.SUCCESSFUL_PROCESSING);

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertArrayEquals(expectedStream.toByteArray(), result);
    }
}
