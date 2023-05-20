package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class RotationTest {
    @Test
    void from_key_rotation() {
        String currentKey = "abc";
        String newKey = "bca";
        PersistenceCipherType cipher = PersistenceCipherType.NONE;
        RotationStrategy rotation = RotationStrategy.createSecretRotationStrategy(cipher, currentKey, newKey);
        
        assertNotNull(rotation);
        assertTrue(rotation.isSecretRotationStrategy());
        assertFalse(rotation.isCipherRotationStrategy());
        assertEquals(rotation.getCurrentCipher(), PersistenceCipherType.NONE);
        assertEquals(rotation.getNewCipher(), PersistenceCipherType.NONE);
    }
    
    @Test
    void from_cipher_rotation() throws IllegalAccessException {
        String currentKey = "abc";
        String newKey = "a".repeat(16);
        RotationStrategy rotation = RotationStrategy.createCipherRotationStrategy(PersistenceCipherType.NONE, PersistenceCipherType.AES_128_GCM_SIV, currentKey, newKey);
        
        assertNotNull(rotation);
        assertFalse(rotation.isSecretRotationStrategy());
        assertTrue(rotation.isCipherRotationStrategy());
        assertEquals(rotation.getCurrentCipher(), PersistenceCipherType.NONE);
        assertEquals(rotation.getNewCipher(), PersistenceCipherType.AES_128_GCM_SIV);
    }
    
    @Test
    void rotate() {
        fail();
    }
    
}
