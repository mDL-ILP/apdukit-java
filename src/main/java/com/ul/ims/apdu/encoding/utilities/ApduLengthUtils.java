package com.ul.ims.apdu.encoding.utilities;

import com.ul.ims.apdu.encoding.Constants;
import com.ul.ims.apdu.encoding.exceptions.ParseException;
import com.ul.ims.apdu.encoding.exceptions.InvalidNumericException;
import com.ul.ims.apdu.extensions.ByteArrayInputStreamExtension;

/**
 * All the utilities methods to get the data or expected length of an apdu.
 * Utils written according to ISO 7816-4.
 * All magic numbers in this class come from the said ISO standard.
 */
public class ApduLengthUtils {

    /**
     * Encode the LE field of the command apdu.
     * English: Parse the expected response length. This is only at the end of the apdu stream.
     * This will not touch the stream
     * See: 5.1 Command-response pairs in ISO 7816-4.
     * @param stream
     * @return length
     * @throws ParseException
     * @throws InvalidNumericException
     */
    public static int decodeMaxExpectedLength(ByteArrayInputStreamExtension stream) throws ParseException {
        Byte firstByte = stream.readByte();
        Byte secondByte = stream.readByte();
        Byte thirdByte = stream.readByte();
        if(firstByte == null) {
            throw new ParseException("Not enough bytes given to read expected length.");
        }

        if(firstByte == 0) {
            //If there are two bytes left in the stream. Read it as a short (extended length)
            if(secondByte != null && thirdByte != null) {
                return ConversionUtils.fromBytesToShort(new byte[]{secondByte, thirdByte });
            }
            //If the byte is set to '00', then Ne is 256.
            if(secondByte == null && thirdByte == null) {
                return 256;
            }
            //If the two bytes are set to '0000', then Ne is 65 536.
            if (secondByte != null && secondByte == 0 && thirdByte == null) {
                return Constants.DEFAULT_MAX_EXPECTED_LENGTH_EXTENDED;
            }
        } else {
            //From '01' to 'FF', the byte encodes Nc from one to 255.
            if(secondByte == null && thirdByte == null) {
                return firstByte;
            }
        }
        throw new ParseException("Unhandled or invalid expected length.");
    }

    /**
     * Encode the data length. This can be in the middle of the apdu stream and takes care of both the extended and normal length type.
     * @param stream
     * @return
     * @throws ParseException
     * @throws InvalidNumericException
     */
    public static short decodeDataLength(ByteArrayInputStreamExtension stream) throws ParseException {
        Byte firstByte = stream.readByte();
        if(firstByte == null) {
            throw new ParseException("Not enough bytes given to read data length.");
        }
        boolean isExtendedLength = firstByte == 0;

        short result = 0;//length
        if(!isExtendedLength && stream.available() == firstByte) {
            result = firstByte;
        } else {
            Byte secondByte = stream.readByte();
            Byte thirdByte = stream.readByte();
            if(secondByte != null && thirdByte != null) {
                result = ConversionUtils.fromBytesToShort(new byte[]{secondByte, thirdByte});
            }
        }
        if(result == 0 || stream.available() != result) {
            throw new ParseException("Supplied length does not match any possible remaining data length");
        }
        return result;
    }

    /**
     * For a given data length encodes it in either 1 byte or 3 bytes depending if the passed length value can be represented using one byte.
     * In the case that value is too large the method will return a short of 2 bytes with a single 0 byte to indicate that it is extended length
     * @param length
     * @return byte array representing the given length.
     */
    public static byte[] encodeDataLength(short length) {
        if (length == 256) {
            return new byte[]{0x00};
        }
        byte[] parts = ConversionUtils.fromShortToBytes(length);
        //Is the first part not 0? Meaning its bigger than one byte? Then use extended length
        if(parts[0] != 0) {
            return new byte[]{0, parts[0], parts[1]};//extended length
        }
        //Not extended length
        return new byte[]{parts[1]};
    }

    public static byte[] encodeMaxExpectedLength(int length) throws InvalidNumericException {
        if(length == Constants.DEFAULT_MAX_EXPECTED_LENGTH_EXTENDED) {
            return new byte[]{0x00, 0x00};
        }
        if (length > Constants.DEFAULT_MAX_EXPECTED_LENGTH_EXTENDED) {
            throw new InvalidNumericException("max expected length overflows extended length.");
        }
        return encodeDataLength((short)length);
    }
}

