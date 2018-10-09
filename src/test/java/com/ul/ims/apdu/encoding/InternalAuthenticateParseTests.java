package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.apps.ExampleApp;
import com.ul.ims.apdu.encoding.CommandApdu;
import com.ul.ims.apdu.encoding.Constants;
import com.ul.ims.apdu.encoding.InternalAuthenticateCommand;
import org.junit.Test;

import static org.junit.Assert.*;

public class InternalAuthenticateParseTests {

//    @Test
//    public void testSelectDFSuccess() throws Exception {
//        byte[] input = {(byte) 0x00, (byte) 0x88, (byte) 0x00, (byte) 0x00,
//                (byte) 0x08, (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x02,
//                (byte) 0x48, (byte) 0x04, (byte) 0x00, (byte) 0x34, (byte) 0x00};
//        CommandApdu command = CommandApdu.fromBytes(input);
//        assertTrue(command instanceof InternalAuthenticateCommand);
//        InternalAuthenticateCommand result = (InternalAuthenticateCommand) command;
//        assertEquals(Constants.DEFAULT_ALGORITHM_INFO, result.getAlgorithmInfo());
//        assertEquals(Constants.DEFAULT_REFERENCE_DATA_QUALIFIER,result.getReferenceDataQualifier());
//        assertNotNull(result.getChallenge());
//        assertEquals(Constants.DEFAULT_CHALLENGE_LENGTH, result.getChallenge().length);
//        assertEquals(256, result.getMaxExpectedLength());
//    }
}
