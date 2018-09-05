package com.ul.ims.apdu.encoding.exceptions;

public class ParseException extends Exception {

    private String description;

    public ParseException(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
