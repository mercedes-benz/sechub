package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.util.Base64;
import java.util.HexFormat;

public class BinaryStringFactory {
    public static BinaryString createFromBytes(byte[] bytes) {
        return createFromBytes(bytes, BinaryStringEncodingType.BASE64);
    }
    
    public static BinaryString createFromBytes(byte[] bytes, BinaryStringEncodingType encodingType) {
        if (bytes == null) {
                throw new IllegalArgumentException("String cannot be null.");
        }
        
        BinaryString binaryString;
        
        switch(encodingType) {
        case BASE64:
            binaryString = new Base64String(bytes);
            break;
        case HEX:
            binaryString = new HexString(bytes);
            break;
        case PLAIN:
            binaryString = new PlainString(bytes);
            break;
        default:
            throw new IllegalArgumentException("Unknown binary string type");
        }
        
        return binaryString;
    }
    
    public static BinaryString createFromString(String string) {
        return createFromString(string, BinaryStringEncodingType.BASE64);
    }
   
    public static BinaryString createFromString(String string, BinaryStringEncodingType encodingType) {
        if (string == null) {
            throw new IllegalArgumentException("String cannot be null.");
        }
        
        BinaryString binaryString;
        
        switch(encodingType) {
        case BASE64:
            binaryString = new Base64String(string);
            break;
        case HEX:
            binaryString = new HexString(string);
            break;
        case PLAIN:
            binaryString = new PlainString(string);
            break;
        default:
            throw new IllegalArgumentException("Unknown binary string type");
        }
        
        return binaryString;
    }
    
    public static BinaryString createFromHex(String stringInHexFormat, BinaryStringEncodingType encodingType) {
        if (stringInHexFormat == null) {
            throw new IllegalArgumentException("String cannot be null.");
        }
        
        HexFormat hexFormat = HexFormat.of();
        byte[] hexBytes = hexFormat.parseHex(stringInHexFormat);
        
        return createFromBytes(hexBytes, encodingType);
    }
    
    public static BinaryString createFromBase64(String stringInBase64Format, BinaryStringEncodingType encodingType) {
        if (stringInBase64Format == null) {
            throw new IllegalArgumentException("String cannot be null.");
        }
        
        // if it cannot be decoded, 
        // it will throw an IllegalArgumentException
        byte[] decoded = Base64.getDecoder().decode(stringInBase64Format);
        
        return createFromBytes(decoded, encodingType);
    }
}
