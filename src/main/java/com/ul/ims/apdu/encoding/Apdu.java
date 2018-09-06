package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Application Protocol Data Unit
 */
public interface Apdu {
    /**
     * Validate Apdu. Each type will throw if values aren't set etc.
     */
    void validate() throws Exception;

    /**
     * Apdu to bytes.
     */
    ByteArrayOutputStream toBytes() throws IOException, InvalidApduException, InvalidNumericException;

}
