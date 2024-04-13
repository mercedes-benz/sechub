package com.mercedesbenz.sechub.commons.core.security.persistence;

/**
 * A wrapper around the string class.
 * 
 * The string class in Java is final and therefore it is impossible to inherited from.
 * 
 * @author Jeremias Eppler
 */
public class PlainString extends AbstractBinaryString {

    PlainString(byte[] bytes) {
        super(bytes);
    }
    
    PlainString(String string) {
        super(string);
    }
    
    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public BinaryStringEncodingType getType() {
        return BinaryStringEncodingType.PLAIN;
    }

    @Override
    public String toString() {
        return new String(bytes);
    }

}
