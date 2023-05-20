package com.mercedesbenz.sechub.commons.core.security.persistence;

import java.util.Base64;
import java.util.Objects;

/**
 * A base64 encoded string.
 * 
 * @author Jeremias Eppler
 *
 */
public class B64String {
    String b64String;

    private B64String(String b64String) {
        this.b64String = b64String;
    }

    public static B64String from(String string) {
        return new B64String(encode(string));
    }
    
    public static B64String from(byte[] bytes) {
        return new B64String(encode(bytes));
    }
    
    public static B64String fromBase64String(String base64String) {
        // if it cannot be decoded, 
        // it will throw an IllegalArgumentException
        Base64.getDecoder().decode(base64String);

        return new B64String(base64String);
    }

    private static String encode(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes());
    }
    
    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String getString() {
        return getBytes().toString();
    }
    
    public byte[] getBytes() {
        return Base64.getDecoder().decode(b64String);
    }

    @Override
    public int hashCode() {
        return Objects.hash(b64String);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        B64String other = (B64String) obj;
        return Objects.equals(b64String, other.b64String);
    }

    @Override
    public String toString() {
        return b64String;
    }
}
