package com.ul.ims.apdu.encoding.types;

import com.ul.ims.apdu.encoding.exceptions.InvalidElementaryFileId;
import com.ul.ims.apdu.encoding.types.ElementaryFileID;
import org.junit.Test;

public class ElementaryFileIDTests {

    @Test
    public void testValidShortFileID() throws Exception {
        new ElementaryFileID((byte)0x01);
    }

    @Test(expected =  InvalidElementaryFileId.class)
    public void testInvalidShortFileID() throws Exception {
        new ElementaryFileID((byte)0x8E);
    }
}
