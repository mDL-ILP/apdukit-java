package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.ValueNotSetException;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InternalAuthenticateCommandBuildTests {
    private InternalAuthenticateCommand subject;

    @Before
    public void runBeforeTestMethod() {
        this.subject = new InternalAuthenticateCommand();
    }

    @Test
    public void testInternalAuthenticateDefaultMaxExpLength() throws Exception{
        //Setup
        subject.setAlgorithmInfo(Constants.DEFAULT_ALGORITHM_INFO);
        subject.setReferenceDataQualifier(Constants.DEFAULT_REFERENCE_DATA_QUALIFIER);
        subject.setChallenge(new byte[8]);

        //Call
        byte[] result = subject.toBytes().toByteArray();

        //Assertion
        assertEquals("InstructionClass", 0, result[0]);
        assertEquals("InstructionCode", (byte)0x88, result[1]);
        assertEquals("algorithmInfo", Constants.DEFAULT_ALGORITHM_INFO, result[2]);
        assertEquals("referenceDataQualifier", Constants.DEFAULT_REFERENCE_DATA_QUALIFIER, result[3]);
        assertEquals("le", 0x08, result[4]);
        assertEquals("data", 0x00, result[5]);
        assertEquals("data", 0x00, result[6]);
        assertEquals("data", 0x00, result[7]);
        assertEquals("data", 0x00, result[8]);
        assertEquals("data", 0x00, result[9]);
        assertEquals("data", 0x00, result[10]);
        assertEquals("data", 0x00, result[11]);
        assertEquals("data", 0x00, result[12]);
        assertEquals("data", 0x00, result[13]);
        assertEquals("data", 0x00, result[14]);
    }

    @Test
    public void testValidation() throws Exception {
        callValidation("Invalid challenge");
        subject.setChallenge(new byte[8]);
        callValidation("algorithmInfo");
        subject.setAlgorithmInfo(Constants.DEFAULT_ALGORITHM_INFO);
        callValidation("referenceDataQualifier");
        subject.setReferenceDataQualifier(Constants.DEFAULT_REFERENCE_DATA_QUALIFIER);
        subject.validate();
    }

    private void callValidation(String value) {
        try {
            subject.validate();
            fail("It should've thrown an exception");
        }catch (ValueNotSetException e) {
            assertEquals(value, e.getValueName());
        }catch (InvalidApduException e) {
            assertEquals(value, e.getDescription());
        } catch (Exception e) {
            fail("It threw the wrong exception");
        }
    }
}
