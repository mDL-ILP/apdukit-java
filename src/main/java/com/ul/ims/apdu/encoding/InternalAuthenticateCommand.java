package com.ul.ims.apdu.encoding;

import com.ul.ims.apdu.encoding.enums.InstructionCode;
import com.ul.ims.apdu.encoding.exceptions.InvalidApduException;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.exceptions.ValueNotSetException;
import com.ul.ims.apdu.encoding.utilities.ApduLengthUtils;
import com.ul.ims.apdu.encoding.utilities.ConversionUtils;
import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InternalAuthenticateCommand extends CommandApdu {
    private Byte algorithmInfo;
    private Byte referenceDataQualifier;
    private byte[] challenge;
    private int maxExpectedLength = Constants.DEFAULT_MAX_EXPECTED_LENGTH_EXTENDED;

    public InternalAuthenticateCommand() {
        super(InstructionCode.INTERNAL_AUTHENTICATE);
    }

    public InternalAuthenticateCommand(ByteArrayInputStreamExtension stream) throws ParseException {
        super(stream);
        this.algorithmInfo = stream.readByte();
        this.referenceDataQualifier = stream.readByte();
        this.decodeChallenge(stream);
        this.decodeMaxExpectedLength(stream);
        validate();
    }

    //Called when reading, parsing
    private void decodeChallenge(ByteArrayInputStreamExtension stream) throws ParseException {
        try {
            int challengeLength = ApduLengthUtils.decodeDataLength(stream);
            this.challenge = stream.readBytes(challengeLength);
        } catch (Exception e) {
            throw new ParseException(e.getMessage());
        }
    }

    //Called when writing, encoding
    private void encodeChallenge(ByteArrayOutputStream stream) throws IOException {
        byte[] challengeLength = ApduLengthUtils.encodeDataLength((short) challenge.length);
        stream.write(challengeLength);//Length
        stream.write(this.challenge);//Data
    }

    private void decodeMaxExpectedLength(ByteArrayInputStreamExtension stream) throws ParseException {
        try {
            this.maxExpectedLength = ApduLengthUtils.decodeMaxExpectedLength(stream);
        } catch (InvalidNumericException e) {
            throw new ParseException(e.getMessage());
        }
    }

    private void encodeMaxExpectedLength(ByteArrayOutputStream stream) throws IOException, InvalidNumericException {
        stream.write(ApduLengthUtils.encodeMaxExpectedLength(this.maxExpectedLength));
    }

    @Override
    public void validate() throws InvalidApduException {
        super.validate();
        if (instructionCode != InstructionCode.INTERNAL_AUTHENTICATE) {
            throw new InvalidApduException("Instruction code is not INTERNAL_AUTHENTICATE");
        }
        if (challenge == null || challenge.length != Constants.DEFAULT_CHALLENGE_LENGTH) {
            throw new InvalidApduException("Invalid challenge");
        }
        if (algorithmInfo == null) {
            throw new ValueNotSetException("algorithmInfo");
        }
        if (referenceDataQualifier == null) {
            throw new ValueNotSetException("referenceDataQualifier");
        }
    }

    @Override
    public ByteArrayOutputStream toBytes() throws InvalidApduException, IOException, InvalidNumericException {
        this.validate();
        ByteArrayOutputStream stream = super.toBytes();
        stream.write(this.algorithmInfo); //P1
        stream.write(this.referenceDataQualifier);//P2
        encodeChallenge(stream);
        encodeMaxExpectedLength(stream);
        return stream;
    }

    //Getters and setters
    public byte getAlgorithmInfo() {
        return algorithmInfo;
    }

    public InternalAuthenticateCommand setAlgorithmInfo(byte algorithmInfo) {
        this.algorithmInfo = algorithmInfo;
        return this;
    }

    public byte getReferenceDataQualifier() {
        return referenceDataQualifier;
    }

    public InternalAuthenticateCommand setReferenceDataQualifier(byte referenceDataQualifier) {
        this.referenceDataQualifier = referenceDataQualifier;
        return this;
    }

    public byte[] getChallenge() {
        return challenge;
    }

    public InternalAuthenticateCommand setChallenge(byte[] challenge) {
        this.challenge = challenge;
        return this;
    }

    public int getMaxExpectedLength() {
        return maxExpectedLength;
    }

    public InternalAuthenticateCommand setMaxExpectedLength(short maxExpectedLength) {
        this.maxExpectedLength = maxExpectedLength;
        return this;
    }
}
