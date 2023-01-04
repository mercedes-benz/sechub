package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.jupiter.api.Test;

public class AesGcmSivTest {
    private static final String LONG_TEXT = "Hello world, this is long text with different emojis. Today, I had for breakfast two 🥐, 1 🥑 and some 🥪. That made me happy ☺️!";
    
    @Test
    void generate_new_createialization_vector() {
        String b64createializationVector = AesGcmSiv.generateNewInitializationVector();
        byte[] createializationVector = Base64.getDecoder().decode(b64createializationVector);

        assertEquals(AesGcmSiv.IV_LENGTH_IN_BYTES, createializationVector.length);
    }

    @Test
    void create_secret_32_bytes() throws InvalidKeyException {
        String secret = Base64.getEncoder().encodeToString("a".repeat(32).getBytes());
        AesGcmSiv crypto = AesGcmSiv.create(secret);

        assertNotNull(crypto);
    }

    @Test
    void create_secret_16_bytes() throws InvalidKeyException {
        String secret = Base64.getEncoder().encodeToString("a".repeat(16).getBytes());
        AesGcmSiv crypto = AesGcmSiv.create(secret);

        assertNotNull(crypto);
    }

    @Test
    void create_secret_secret_6_bytes_invalid() {
        String secret = Base64.getEncoder().encodeToString("abcdef".getBytes());

        assertThrows(InvalidKeyException.class, () -> {
            AesGcmSiv.create(secret);
        });
    }

    @Test
    void create_secret_secret_31_bytes_invalid() {
        String secret = Base64.getEncoder().encodeToString("a".repeat(31).getBytes());

        assertThrows(InvalidKeyException.class, () -> {
            AesGcmSiv.create(secret);
        });
    }

    @Test
    void encrypt__aes_256() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        String secret = Base64.getEncoder().encodeToString("w".repeat(32).getBytes());
        String plainText = "bca";
        String expectedCipherText = "1qKKtEpM2ppl4wWrJxJo0MiFdw==";
        String b64createializationVector = repeatStringBase64Encoded("i", AesGcmSiv.IV_LENGTH_IN_BYTES);

        AesGcmSiv crypto = AesGcmSiv.create(secret);
        String cipherText = crypto.encrypt(plainText, b64createializationVector);

        assertEquals(expectedCipherText, cipherText);
    }

    @Test
    void encrypt__aes_256_createialization_vector_too_short() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = Base64.getEncoder().encodeToString("w".repeat(32).getBytes());
        String plainText = "bca";
        String b64createializationVector = repeatStringBase64Encoded("abc", 2);

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        Exception exception = assertThrows(InvalidAlgorithmParameterException.class, () -> {
            crypto.encrypt(plainText, b64createializationVector);
        });

        assertEquals("Invalid nonce", exception.getMessage());
    }

    @Test
    void encrypt__aes_256_createialization_vector_too_long() throws InvalidKeyException, InvalidAlgorithmParameterException {
        String secret = Base64.getEncoder().encodeToString("w".repeat(32).getBytes());
        String plainText = "bca";
        String b64createializationVector = repeatStringBase64Encoded("abc", 50);

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        Exception exception = assertThrows(InvalidAlgorithmParameterException.class, () -> {
            crypto.encrypt(plainText, b64createializationVector);
        });

        assertEquals("Invalid nonce", exception.getMessage());
    }

    @Test
    void decrypt__aes_256() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        String secret = Base64.getEncoder().encodeToString("w".repeat(32).getBytes());
        String expectedPlainText = "bca";
        String cipherText = "1qKKtEpM2ppl4wWrJxJo0MiFdw==";
        String b64createializationVector = repeatStringBase64Encoded("i", AesGcmSiv.IV_LENGTH_IN_BYTES);

        AesGcmSiv crypto = AesGcmSiv.create(secret);
        String plainText = crypto.decrypt(cipherText, b64createializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void encrypt__aes_128() throws InvalidKeyException, InvalidAlgorithmParameterException {
        String secret = repeatStringBase64Encoded("a", 16);
        String plainText = "bca";
        String expectedCipherText = "yGcKhuWbewS+R4tlegECshiTSQ==";
        String b64createializationVector = repeatStringBase64Encoded("i", AesGcmSiv.IV_LENGTH_IN_BYTES);

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        String cipherText = crypto.encrypt(plainText, b64createializationVector);

        assertEquals(expectedCipherText, cipherText);
    }

    @Test
    void decrypt__aes_128() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 16);
        String expectedPlainText = "bca";
        String b64CipherText = "yGcKhuWbewS+R4tlegECshiTSQ==";
        String b64createializationVector = repeatStringBase64Encoded("i", AesGcmSiv.IV_LENGTH_IN_BYTES);

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        String plainText = crypto.decrypt(b64CipherText, b64createializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void decrypt__aes_128_wrong_cipher_text() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 16);
        String b64CipherText = Base64.getEncoder().encodeToString("hello world, this is base 64 encoded".getBytes());
        String b64createializationVector = repeatStringBase64Encoded("i", AesGcmSiv.IV_LENGTH_IN_BYTES);

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        Exception exception = assertThrows(AEADBadTagException.class, () -> {
            crypto.decrypt(b64CipherText, b64createializationVector);
        });

        assertEquals("mac check failed", exception.getMessage());
    }

    @Test
    void decrypt__aes_256_wrong_cipher_text() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 32);
        String b64CipherText = Base64.getEncoder().encodeToString("hello world, this is base 64 encoded".getBytes());
        String b64createializationVector = repeatStringBase64Encoded("i", AesGcmSiv.IV_LENGTH_IN_BYTES);

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        Exception exception = assertThrows(AEADBadTagException.class, () -> {
            crypto.decrypt(b64CipherText, b64createializationVector);
        });

        assertEquals("mac check failed", exception.getMessage());
    }

    
    @Test
    void encrypt__aes_128_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 16);
        String plainText = LONG_TEXT;
        String expectedCipherText = "28/RdEWgYbpbiraiWcSo58+8sfCRRQpSoZiiFqNsYN8tLLVE6AXeQjxh4zazK65G7T0dmFnqrbyx6aRUB+7I6guFXMxqjRij9HdRkae4OalWZVNtCs2+mjBBMNOB5Ke2bgIcYDZbDMRWceBtJnE5PKg7vxrNFgR+8uFw9ejbRVxzGTbkyNeh48QVT9Knk7LpmqQ/eHFTvsvnD0M=";
        String b64createializationVector = repeatStringBase64Encoded("i", AesGcmSiv.IV_LENGTH_IN_BYTES);

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        String cipherText = crypto.encrypt(plainText, b64createializationVector);

        assertEquals(expectedCipherText, cipherText);
    }

    @Test
    void encrypt__aes_256_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 32);
        String plainText = LONG_TEXT;
        String expectedCipherText = "E2RrqhXtKG39okWxxvw3d4NQ+DXr2+Qa78JvdpHS4+FOckRECTkjoX2JfZNKHP3on0sDO1q8uTc+BY9QJkMK+MsWzp8YT4SR0UxWo7uy5SSPMXOLLcQg0vzTOTdgo00vPQy34vogNYO1V/TTzOzzP6Ng0kT9TDsYUWu+v0y3uZw/ujl2X8bP8Nfrp2ZRMrgfpj7NbjQQd8hD5AY=";
        String b64createializationVector = repeatStringBase64Encoded("i", AesGcmSiv.IV_LENGTH_IN_BYTES);

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        String cipherText = crypto.encrypt(plainText, b64createializationVector);

        assertEquals(expectedCipherText, cipherText);
    }

    @Test
    void decrypt__aes_128_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 16);
        String expectedPlainText = LONG_TEXT;
        String cipherText = "28/RdEWgYbpbiraiWcSo58+8sfCRRQpSoZiiFqNsYN8tLLVE6AXeQjxh4zazK65G7T0dmFnqrbyx6aRUB+7I6guFXMxqjRij9HdRkae4OalWZVNtCs2+mjBBMNOB5Ke2bgIcYDZbDMRWceBtJnE5PKg7vxrNFgR+8uFw9ejbRVxzGTbkyNeh48QVT9Knk7LpmqQ/eHFTvsvnD0M=";
        String b64createializationVector = repeatStringBase64Encoded("i", AesGcmSiv.IV_LENGTH_IN_BYTES);

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        String plainText = crypto.decrypt(cipherText, b64createializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void decrypt__aes_256_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 32);
        String expectedPlainText = LONG_TEXT;
        String cipherText = "E2RrqhXtKG39okWxxvw3d4NQ+DXr2+Qa78JvdpHS4+FOckRECTkjoX2JfZNKHP3on0sDO1q8uTc+BY9QJkMK+MsWzp8YT4SR0UxWo7uy5SSPMXOLLcQg0vzTOTdgo00vPQy34vogNYO1V/TTzOzzP6Ng0kT9TDsYUWu+v0y3uZw/ujl2X8bP8Nfrp2ZRMrgfpj7NbjQQd8hD5AY=";
        String b64createializationVector = repeatStringBase64Encoded("i", AesGcmSiv.IV_LENGTH_IN_BYTES);

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        String plainText = crypto.decrypt(cipherText, b64createializationVector);

        assertEquals(expectedPlainText, plainText);
    }
    
    @Test
    void getCiphers_aes_256() throws InvalidKeyException {
        String secret = repeatStringBase64Encoded("a", 32);
        AesGcmSiv crypto = AesGcmSiv.create(secret);
        
        assertEquals(PersistenceCipherType.AES_256_GCM_SIV, crypto.getCipher());
    }
    
    @Test
    void getCiphers_aes_128() throws InvalidKeyException {
        String secret = repeatStringBase64Encoded("a", 16);
        AesGcmSiv crypto = AesGcmSiv.create(secret);
        
        assertEquals(PersistenceCipherType.AES_128_GCM_SIV, crypto.getCipher());
    }

    private String repeatStringBase64Encoded(String sequence, int times) {
        return Base64.getEncoder().encodeToString((sequence.repeat(times)).getBytes());
    }
}
