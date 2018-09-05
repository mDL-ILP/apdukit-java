package com.ul.ims.apdu.encoding.validation;

import com.ul.ims.apdu.encoding.exceptions.InvalidElementaryFileId;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import org.junit.Test;

public class ElementaryFileIDValidationUnitTest {

    @Test
    public void validShortFileID() throws Exception {
        new ElementaryFileID((byte)0x01);
    }

    @Test(expected =  InvalidElementaryFileId.class)
    public void invalidShortFileID() throws Exception {
        new ElementaryFileID((byte)0x8E);
    }
}
