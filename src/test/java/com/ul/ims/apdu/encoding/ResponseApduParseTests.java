package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.enums.StatusCode;

import org.junit.Test;

import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class ResponseApduParseTests {

    @Test
    public void testSuccessfulProcessing() throws Exception{
        //Setup
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(new byte[] {(byte) 0x90, (byte) 0x00});

        //Call
        ResponseApdu result = new ResponseApdu(stream);

        //Assertion
        assertArrayEquals(null, result.getData());
        assertEquals(StatusCode.SUCCESSFUL_PROCESSING, result.getStatusCode());
    }

    //Scenario: response to  (READ BINARY command: '00 B0 81 00 06')
    @Test
    public void testSuccessfulProcessingWithData() throws Exception{
        //Setup
        byte[] data = new byte[] {(byte) 0x71, (byte) 0x82, (byte) 0x01, (byte) 0x05, (byte) 0x80, (byte) 0x01};

        ByteArrayOutputStream buildStream = new ByteArrayOutputStream();
        buildStream.write(data);
        buildStream.write(new byte[]{(byte) 0x90, (byte) 0x00});
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(buildStream.toByteArray());

        //Call
        ResponseApdu result = new ResponseApdu(stream);

        //Assertion
        assertArrayEquals(data, result.getData());
        assertEquals(StatusCode.SUCCESSFUL_PROCESSING, result.getStatusCode());
    }

}
