package com.ul.ims.apdu.interpreter.Exceptions;

import com.ul.ims.apdu.encoding.enums.StatusCode;

public class ResponseApduStatusCodeError extends Exception {

    StatusCode code;

    public StatusCode getCode() {
        return code;
    }

    public ResponseApduStatusCodeError(StatusCode code) {
        super(String.format("Responder sent back an error status code: %d", code.getShortValue()));
        this.code = code;
    }

}
