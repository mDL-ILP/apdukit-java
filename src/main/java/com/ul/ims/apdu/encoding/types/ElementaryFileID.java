package com.ul.ims.apdu.encoding.types;

import com.ul.ims.apdu.encoding.exceptions.InvalidElementaryFileId;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.encoding.utilities.ConversionUtils;
import java.util.Arrays;

//ElementaryFileID, identifying a certain file (datagroup). ISO 7816-4, Section 5 - Basic Organizations
public class ElementaryFileID implements FileID {
    private final Byte shortIdentifier;
    private final byte[] normalIdentifier;

    public ElementaryFileID(Byte shortIdentifier, short normalIdentifier) throws InvalidElementaryFileId {
        this.shortIdentifier = shortIdentifier;
        this.normalIdentifier = ConversionUtils.fromShortToBytes(normalIdentifier);
        this.validate();
    }

    public ElementaryFileID(Byte shortIdentifier) throws InvalidElementaryFileId {
        this.shortIdentifier = shortIdentifier;
        this.normalIdentifier = null;
        this.validate();
    }

    public byte getShortIdentifier() {
        return shortIdentifier;
    }

    public boolean isShortIDAvailable() {
        return shortIdentifier != null;
    }

    public byte[] getNormalIdentifier() {
        return normalIdentifier;
    }

    public short getShortIDValueAsAShort() throws InvalidNumericException {
        if(!this.isShortIDAvailable()) {
            throw new InvalidNumericException("The short id is not available");
        }
        return ConversionUtils.fromBytesToShort(new byte[]{this.shortIdentifier});
    }

    public short getNormalIDValueAsAShort() throws InvalidNumericException {
        return ConversionUtils.fromBytesToShort(this.normalIdentifier);
    }

    @Override
    public byte[] getValue() {
        return normalIdentifier;
    }

    private void validate() throws InvalidElementaryFileId {
        if(this.shortIdentifier == null && this.normalIdentifier == null) {
            throw new InvalidElementaryFileId("no identifier is available.");
        }
        //Checks for the short file identifier
        if(this.shortIdentifier != null) {
            if(!ConversionUtils.areTheFirstThreeBitsZero(this.shortIdentifier)) {
                throw new InvalidElementaryFileId("The first three bits have to be 0");
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if( !(obj instanceof ElementaryFileID) ) {
            return false;
        }
        byte otherShortID = ((ElementaryFileID) obj).shortIdentifier;
        byte[] otherNormalID = ((ElementaryFileID) obj).normalIdentifier;
        return (this.shortIdentifier == otherShortID || Arrays.equals(this.normalIdentifier,otherNormalID));
    }

}
