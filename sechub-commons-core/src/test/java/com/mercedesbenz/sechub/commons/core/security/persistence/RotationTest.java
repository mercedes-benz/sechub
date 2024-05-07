package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.junit.jupiter.api.Test;

public class RotationTest {
    @Test
    void secret_rotation_cipher_none()
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        BinaryString currentSecret = new PlainString("abc");
        BinaryString newSecret = new PlainString("bca");
        BinaryString cipherText = new PlainString("hello");
        BinaryString initializationVector = new PlainString("iv");

        PersistenceCipherType cipherType = PersistenceCipherType.NONE;
        RotationStrategy rotation = RotationStrategy.createSecretRotationStrategy(currentSecret, newSecret, cipherType);

        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector);

        /* test */
        assertNotNull(rotation);
        assertEquals(cipherText, newCipherText);
        assertTrue(rotation.isSecretRotationStrategy());
        assertFalse(rotation.isCipherRotationStrategy());
        assertEquals(PersistenceCipherType.NONE, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.NONE, rotation.getNewCipher());
    }

    @Test
    void secret_rotation_cipher_aes_gcm_siv_128()
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlainText = "Hello, I am ☺️ 4 you.";
        BinaryString expectedCipherText = BinaryStringFactory.createFromBase64("8gWa4YPRlshBZCml8a0xvAJ1Y1mNU9iovclpvOhwVj4XiiaZvWKHWkU=",
                BinaryStringEncodingType.BASE64);
        BinaryString currentSecret = new PlainString("a".repeat(16));
        BinaryString newSecret = new PlainString("z".repeat(16));
        BinaryString cipherText = BinaryStringFactory.createFromBase64("DuwfqoAJrzZiK3u5v0XEnARPOjLugpobvWCxfTV6Y1FkAOECII/J8RU=",
                BinaryStringEncodingType.BASE64);
        BinaryString initializationVector = new PlainString("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        PersistenceCipherType cipherType = PersistenceCipherType.AES_GCM_SIV_128;
        RotationStrategy rotation = RotationStrategy.createSecretRotationStrategy(currentSecret, newSecret, cipherType);

        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector);

        /* test */
        assertNotNull(rotation);
        assertEquals(expectedCipherText, newCipherText);
        assertTrue(rotation.isSecretRotationStrategy());
        assertFalse(rotation.isCipherRotationStrategy());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, rotation.getNewCipher());

        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, newSecret);
        String plainText = cipher.decrypt(newCipherText, initializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void secret_rotation_cipher_aes_gcm_siv_256()
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlainText = "Hello, I am ☺️ 4 you.";
        BinaryString expectedCipherText = BinaryStringFactory.createFromBase64("y5/I7wBmKwqKKazHrRnj2j6v9uuySiHJ9MK3pUvWjkUIC/3jDSFJLPI=",
                BinaryStringEncodingType.BASE64);
        BinaryString currentSecret = new PlainString("a".repeat(32));
        BinaryString newSecret = new PlainString("z".repeat(32));
        BinaryString cipherText = BinaryStringFactory.createFromBase64("KSIAj+JAD95o77GF91GQShbuh0dyuIMdDjhX1VQFi7DW2KSutD0uOt8=",
                BinaryStringEncodingType.BASE64);
        BinaryString initializationVector = new HexString("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        PersistenceCipherType cipherType = PersistenceCipherType.AES_GCM_SIV_256;
        RotationStrategy rotation = RotationStrategy.createSecretRotationStrategy(currentSecret, newSecret, cipherType);

        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector);

        /* test */
        assertNotNull(rotation);
        assertEquals(expectedCipherText, newCipherText);
        assertTrue(rotation.isSecretRotationStrategy());
        assertFalse(rotation.isCipherRotationStrategy());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_256, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_256, rotation.getNewCipher());

        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, newSecret);
        String plainText = cipher.decrypt(newCipherText, initializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void cipher_and_secret_rotation_cipher_none_to_cipher_aes_gcm_siv_128()
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlainText = "Hello, I am ☺️ 4 you.";
        BinaryString expectedCipherText = BinaryStringFactory.createFromBase64("DuwfqoAJrzZiK3u5v0XEnARPOjLugpobvWCxfTV6Y1FkAOECII/J8RU=",
                BinaryStringEncodingType.BASE64);
        BinaryString currentSecret = new PlainString("abc");
        BinaryString newSecret = new PlainString("a".repeat(16));
        BinaryString cipherText = new PlainString(expectedPlainText);
        BinaryString initializationVector = new PlainString("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        PersistenceCipherType currentCipherType = PersistenceCipherType.NONE;
        PersistenceCipherType newCipherType = PersistenceCipherType.AES_GCM_SIV_128;
        RotationStrategy rotation = RotationStrategy.createCipherAndSecretRotationStrategy(currentSecret, newSecret, currentCipherType, newCipherType);

        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector);

        /* test */
        assertNotNull(rotation);
        assertEquals(expectedCipherText, newCipherText);
        assertTrue(rotation.isSecretRotationStrategy());
        assertTrue(rotation.isCipherRotationStrategy());
        assertEquals(PersistenceCipherType.NONE, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, rotation.getNewCipher());

        PersistenceCipher cipher = PersistenceCipherFactory.create(newCipherType, newSecret);
        String plainText = cipher.decrypt(newCipherText, initializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void cipher_and_secret_rotation_cipher_aes_gcm_siv_128_to_cipher_aes_gcm_siv_256()
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlainText = "Hello 👋, welcome to 🌐.";

        BinaryString expectedCipherText = BinaryStringFactory
                .createFromHex("d09be77ddd8dbac86b69b0f5f554faef740555ac93f12aedfdf62700e4ea3016e03dacc105f32f114791d8e6", BinaryStringEncodingType.BASE64);
        BinaryString currentSecret = new Base64String("🍐🍌🍓🍉");
        BinaryString newSecret = new HexString("🥦🥕🥔🫘🥒🫑🌽🍆");
        BinaryString cipherText = BinaryStringFactory.createFromBase64("Qu7ICJBGMw9dAPPBWx86e5bjOq3YKC+x25n/YkluWZAGdSna08tKaE78pMk=",
                BinaryStringEncodingType.BASE64);
        BinaryString initializationVector = new PlainString("🧅".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES / 4));

        PersistenceCipherType currentCipherType = PersistenceCipherType.AES_GCM_SIV_128;
        PersistenceCipherType newCipherType = PersistenceCipherType.AES_GCM_SIV_256;
        RotationStrategy rotation = RotationStrategy.createCipherAndSecretRotationStrategy(currentSecret, newSecret, currentCipherType, newCipherType);

        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector);

        /* test */
        assertNotNull(rotation);
        assertEquals(expectedCipherText, newCipherText);
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_256, rotation.getNewCipher());
        assertTrue(rotation.isSecretRotationStrategy());
        assertTrue(rotation.isCipherRotationStrategy());

        PersistenceCipher cipher = PersistenceCipherFactory.create(newCipherType, newSecret);
        String plainText = cipher.decrypt(newCipherText, initializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void cipher_and_secret_rotation_aes_gcm_siv_256_to_none()
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlainText = "Hello 👋, welcome to 🌐.";

        BinaryString expectedCipherText = BinaryStringFactory.createFromString(expectedPlainText, BinaryStringEncodingType.PLAIN);
        BinaryString currentSecret = new HexString("🥦🥕🥔🫘🥒🫑🌽🍆");
        BinaryString newSecret = new HexString("abc");
        BinaryString cipherText = BinaryStringFactory.createFromHex("d09be77ddd8dbac86b69b0f5f554faef740555ac93f12aedfdf62700e4ea3016e03dacc105f32f114791d8e6",
                BinaryStringEncodingType.HEX);
        BinaryString initializationVector = new Base64String("🧅".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES / 4));

        PersistenceCipherType currentCipherType = PersistenceCipherType.AES_GCM_SIV_256;
        PersistenceCipherType newCipherType = PersistenceCipherType.NONE;
        RotationStrategy rotation = RotationStrategy.createCipherAndSecretRotationStrategy(currentSecret, newSecret, currentCipherType, newCipherType);

        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector);

        /* test */
        assertNotNull(rotation);
        assertEquals(expectedCipherText, newCipherText);
        assertEquals(PersistenceCipherType.AES_GCM_SIV_256, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.NONE, rotation.getNewCipher());
        assertTrue(rotation.isSecretRotationStrategy());
        assertTrue(rotation.isCipherRotationStrategy());

        PersistenceCipher cipher = PersistenceCipherFactory.create(newCipherType, newSecret);
        String plainText = cipher.decrypt(newCipherText, initializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void initializationVector_rotation_cipher_none()
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        BinaryString secret = new PlainString("abc");
        BinaryString cipherText = new PlainString("hello");
        BinaryString initializationVector = new PlainString("vi");
        BinaryString newInitializationVector = new PlainString("iv");

        PersistenceCipherType cipherType = PersistenceCipherType.NONE;
        RotationStrategy rotation = RotationStrategy.createInitializationVectorOnlyRotationStrategy(secret, cipherType);

        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector, newInitializationVector);

        /* test */
        assertNotNull(rotation);
        assertEquals(cipherText, newCipherText);
        assertFalse(rotation.isSecretRotationStrategy());
        assertFalse(rotation.isCipherRotationStrategy());
        assertEquals(PersistenceCipherType.NONE, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.NONE, rotation.getNewCipher());
    }

    @Test
    void initializationVector_rotation_aes_gcm_siv_128()
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlainText = "The quick brown fox jumps over the lazy dog.";

        BinaryString expectedCipherText = BinaryStringFactory
                .createFromBase64("roRrChR9D/VIaebHZgxGG3tYuCR+yXjPleQr4bDZ7B5qsVzC7EnXTi3rT3qDv09t62W/W4V9CIfQRlWs");
        BinaryString secret = new PlainString("d".repeat(16));
        BinaryString cipherText = BinaryStringFactory.createFromBase64("h3Pz6Kue6V4CUYtkZdtWml0vmXkEXjSpXyud98Z3NJjNGk+qFYJmpLCoyO+qBxWKQfxbmnmup9+vjYJQ");
        BinaryString initializationVector = new HexString("vi".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES / 2));
        BinaryString newInitializationVector = new Base64String("iv".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES / 2));

        PersistenceCipherType cipherType = PersistenceCipherType.AES_GCM_SIV_128;
        RotationStrategy rotation = RotationStrategy.createInitializationVectorOnlyRotationStrategy(secret, cipherType);

        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector, newInitializationVector);

        /* test */
        assertNotNull(rotation);
        assertEquals(expectedCipherText, newCipherText);
        assertFalse(rotation.isSecretRotationStrategy());
        assertFalse(rotation.isCipherRotationStrategy());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, rotation.getNewCipher());

        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, secret);
        String plainText = cipher.decrypt(newCipherText, newInitializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void initializationVector_rotation_aes_gcm_siv_256()
            throws InvalidKeyException, IllegalArgumentException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        String expectedPlainText = "Victor jagt zwölf Boxkämpfer quer über den großen Sylter Deich";

        BinaryString expectedCipherText = BinaryStringFactory
                .createFromBase64("0eG35+7x+nr4MbsgRsTqOEjRReF6JzM2thWe+3zHleta1cCXzyCxQJ5Jaxeq7TnYDDg3nR/RcQdc00UlqEJw3WODaffeETDJ0bk5LfP19FWr/g==");
        BinaryString secret = new PlainString("ThisIsAStringWith32Characters!?!");
        BinaryString cipherText = BinaryStringFactory
                .createFromBase64("AfVfrXdv8JImZ9qrpO7vOJRwveOaYRJNsQaeJiuDg5nX3ZT13EwH7CXlZa8IBNvhrqUvEn3w0L0J+JjYVYPxh80CqCpgjbMl4a8Ql5eyqKQIZQ==");
        BinaryString initializationVector = new HexString("vi".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES / 2));
        BinaryString newInitializationVector = new Base64String("iv".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES / 2));

        PersistenceCipherType cipherType = PersistenceCipherType.AES_GCM_SIV_256;
        RotationStrategy rotation = RotationStrategy.createInitializationVectorOnlyRotationStrategy(secret, cipherType);

        /* execute */
        BinaryString newCipherText = rotation.rotate(cipherText, initializationVector, newInitializationVector);

        /* test */
        assertNotNull(rotation);
        assertEquals(expectedCipherText, newCipherText);
        assertFalse(rotation.isSecretRotationStrategy());
        assertFalse(rotation.isCipherRotationStrategy());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_256, rotation.getCurrentCipher());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_256, rotation.getNewCipher());

        PersistenceCipher cipher = PersistenceCipherFactory.create(cipherType, secret);
        String plainText = cipher.decrypt(newCipherText, newInitializationVector);

        assertEquals(expectedPlainText, plainText);
    }
}
