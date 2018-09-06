package com.ul.ims.apdu.encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.ul.ims.apdu.encoding.enums.InstructionClass;
import com.ul.ims.apdu.encoding.enums.InstructionCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.exceptions.ValueNotSetException;
import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;

/**
 * CommandApdu is an abstract class that defines the base apdu request. Selects, reads etc.
 *
 */
public abstract class CommandApdu implements Apdu {
    InstructionClass instructionClass = InstructionClass.DEFAULT;
    InstructionCode instructionCode;

    CommandApdu(ByteArrayInputStreamExtension stream) throws InvalidApduException {
        this.instructionClass = InstructionClass.valueOf(stream.readByte());
        this.instructionCode = InstructionCode.valueOf(stream.readByte());
    }

    CommandApdu(InstructionCode instructionCode) {
        this.instructionCode = instructionCode;
    }

    /**
     * Validate Apdu. Each type will throw if values aren't set etc.
     */
    public void validate() throws InvalidApduException {
        if(instructionClass == null) {
            throw new ValueNotSetException("instructionClass");
        }
        if(instructionCode == null) {
            throw new ValueNotSetException("instructionCode");
        }
    }

    /**
     * Apdu from bytes. Routes and initializes the right apdu subclass depending on the instructionCode
     */
    public static CommandApdu fromBytes(byte[] buf) throws ParseException {
        ByteArrayInputStreamExtension stream = new ByteArrayInputStreamExtension(buf);
        if(stream.available() < 4) {
            throw new InvalidApduException("data should be at least 4 long");
        }
        stream.skip(1);//Skip instruction class.
        InstructionCode instructionCode = InstructionCode.valueOf((byte)stream.readByte());
        if(instructionCode == null) {
            throw new ParseException("Instruction code byte could not be mapped to the instructionCode enum");
        }
        stream.reset();
        switch (instructionCode) {
            case SELECT:
                return new SelectCommand(stream);
            case READ_BINARY:
                return ReadBinaryCommand.fromBytes(stream);
            case INTERNAL_AUTHENTICATE:
                return new InternalAuthenticateCommand(stream);
        }
        return null;
    }

    /**
     * Apdu to bytes.
     */
    public ByteArrayOutputStream toBytes() throws IOException, InvalidApduException, InvalidNumericException {
        this.validate();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(instructionClass.getValue());
        stream.write(instructionCode.getValue());
        return stream;
    }
}
