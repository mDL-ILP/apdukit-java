package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.ValueNotSetException;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InternalAuthenticateBuildTests {
    private InternalAuthenticateCommand subject;

    @Before
    public void runBeforeTestMethod() {
        this.subject = new InternalAuthenticateCommand();
    }

    @Test
    public void testInternalAuthenticateDefaultMaxExpLength() throws Exception{
        subject.setAlgorithmInfo(Constants.DEFAULT_ALGORITHM_INFO);
        subject.setReferenceDataQualifier(Constants.DEFAULT_REFERENCE_DATA_QUALIFIER);
        subject.setChallenge(new byte[8]);
        byte[] expected = new byte[]{(byte) 0x00, (byte) 0x88, (byte) 0x00, (byte) 0x00,
                (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

        byte[] result = subject.toBytes().toByteArray();

        assertArrayEquals(expected, result);
    }

    @Test()
    public void testValidation() throws Exception {
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
        } catch (Exception e) {
            fail("It threw the wrong exception");
        }
    }
}
