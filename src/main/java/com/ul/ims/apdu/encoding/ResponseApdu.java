package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.enums.StatusCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.exceptions.ValueNotSetException;
import com.ul.ims.apdu.encoding.utilities.ConversionUtils;

import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ResponseApdu implements Apdu {
    private StatusCode statusCode;
    private byte[] data;

    public ResponseApdu(ByteArrayInputStreamExtension stream) throws InvalidApduException {
        this.decodeData(stream);
        this.decodeStatusCode(stream);
        validate();
    }

    public ResponseApdu() {
        super();
    }

    public ResponseApdu setData(byte[] data) {
        this.data = data;
        return this;
    }

    public ResponseApdu setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public byte[] getData() {
        return data;
    }

    private void encodeStatusCode(ByteArrayOutputStream stream) throws IOException {
        stream.write(statusCode.getValue());
    }

    private void decodeStatusCode(ByteArrayInputStreamExtension stream) throws InvalidApduException {
        try {
            if (stream.available() != 2) {
                throw new InvalidApduException("Expected to read StatusCode. But remaining buffer is not long enough");
            }
            byte[] status = stream.readBytes(Short.BYTES);
            this.statusCode = StatusCode.valueOf(ConversionUtils.fromBytesToShort(status));
        }catch (InvalidApduException e) {
            throw e;
        }catch (Exception e) {
            throw new InvalidApduException(e.getMessage());
        }
    }

    private void encodeData(ByteArrayOutputStream stream) throws IOException {
        if(data != null) {
            stream.write(data);
        }
    }

    private void decodeData(ByteArrayInputStreamExtension stream) throws InvalidApduException {
        try {
            boolean issetData = stream.available() > Constants.SIZE_RESPONSE_STATUS_CODE;
            if(issetData){
                this.data = stream.readBytes(stream.available() - Constants.SIZE_RESPONSE_STATUS_CODE);//Read all the data except the last two bytes which are reserved for the status code.
            }
        } catch (Exception e) {
            throw new InvalidApduException(e.getMessage());
        }
    }

    @Override
    public void validate() throws ValueNotSetException {
        if(statusCode == null) {
            throw new ValueNotSetException("statusCode");
        }
    }

    @Override
    public ByteArrayOutputStream toBytes() throws IOException, InvalidApduException {
        validate();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.encodeData(stream);
        this.encodeStatusCode(stream);
        return stream;
    }

}
