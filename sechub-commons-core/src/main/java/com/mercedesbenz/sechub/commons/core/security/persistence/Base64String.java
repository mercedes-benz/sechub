package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.util.Base64;

/**
 * A base64 encoded string.
 *
 * @author Jeremias Eppler
 *
 */
public class Base64String extends AbstractBinaryString {

    Base64String(byte[] bytes) {
        super(bytes);
    }

    Base64String(String string) {
        super(string);
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + java.util.Arrays.hashCode(bytes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Base64String other = (Base64String) obj;
        return java.util.Arrays.equals(bytes, other.bytes);
    }

    @Override
    public BinaryStringEncodingType getType() {
        return BinaryStringEncodingType.BASE64;
    }
}
