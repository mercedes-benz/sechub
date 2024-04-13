package com.mercedesbenz.sechub.commons.core.security.persistence;

/**
 * Encoding type of a binary string.
 * 
 * @see BinaryString
 */
public enum BinaryStringEncodingType {
    /**
     * A string with no encoding.
     */
    PLAIN,
    
    /**
     * A string encoded in hexadecimal format
     */
    HEX,
    
    /**
     * A string encoded in base64 format
     */
    BASE64,
}
