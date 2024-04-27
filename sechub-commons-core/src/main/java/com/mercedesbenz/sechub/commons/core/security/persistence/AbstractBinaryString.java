package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.util.Arrays;

/**
 * @author Jeremias Eppler
 */
public abstract class AbstractBinaryString implements BinaryString {
    byte[] bytes;

    protected AbstractBinaryString(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Byte array cannot be null.");
        }
        
        this.bytes = bytes;
    }
    
    protected AbstractBinaryString(String string) {
        if (string == null) {
            throw new IllegalArgumentException("String cannot be null.");
        }
        
        this.bytes = string.getBytes();
    }
    
    public abstract String toString();
    
    public byte[] getBytes() {
        // deep copy
        return Arrays.copyOf(bytes, bytes.length);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(bytes);
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
        AbstractBinaryString other = (AbstractBinaryString) obj;
        return Arrays.equals(bytes, other.bytes);
    }
}
