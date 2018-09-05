package com.ul.ims.apdu.encoding.exceptions;

public class ValueNotSetException extends InvalidApduException {

    public String getValueName() {
        return valueName;
    }

    private String valueName;

    public ValueNotSetException(String valueName) {
        super("Value: "+valueName+" is not set");
        this.valueName = valueName;
    }
}
