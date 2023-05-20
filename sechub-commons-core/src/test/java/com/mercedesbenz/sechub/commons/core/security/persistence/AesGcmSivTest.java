package com.mercedesbenz.sechub.commons.core.security.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.jupiter.api.Test;

public class AesGcmSivTest {
    private static final String LONG_TEXT = "Hello world, this is long text with different emojis. Today, I had for breakfast two 🥐, 1 🥑 and some 🥪. That made me happy ☺️!";
    
    @Test
    void generate_new_createialization_vector() {
        B64String initializationVector = AesGcmSiv.generateNewInitializationVector();

        assertEquals(AesGcmSiv.IV_LENGTH_IN_BYTES, initializationVector.getBytes().length);
    }

    @Test
    void create_secret_32_bytes() throws InvalidKeyException {
        B64String secret = B64String.from("a".repeat(32));
        AesGcmSiv crypto = AesGcmSiv.create(secret);

        assertNotNull(crypto);
    }

    @Test
    void create_secret_16_bytes() throws InvalidKeyException {
        B64String secret = B64String.from("a".repeat(16));
        AesGcmSiv crypto = AesGcmSiv.create(secret);

        assertNotNull(crypto);
    }

    @Test
    void create_secret_secret_6_bytes_invalid() {
        B64String secret = B64String.from("abcdef");

        assertThrows(InvalidKeyException.class, () -> {
            AesGcmSiv.create(secret);
        });
    }

    @Test
    void create_secret_secret_31_bytes_invalid() {
        B64String secret = B64String.from("a".repeat(31));

        assertThrows(InvalidKeyException.class, () -> {
            AesGcmSiv.create(secret);
        });
    }

    @Test
    void encrypt__aes_256() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        B64String secret = B64String.from("w".repeat(32));
        String plainText = "bca";
        String expectedCipherText = "1qKKtEpM2ppl4wWrJxJo0MiFdw==";
        B64String initializationVector = B64String.from("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv crypto = AesGcmSiv.create(secret);
        B64String cipherText = crypto.encrypt(plainText, initializationVector);

        assertEquals(expectedCipherText, cipherText.toString());
    }

    @Test
    void encrypt__aes_256_initialization_vector_too_short() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        B64String secret = B64String.from("w".repeat(32));
        String plainText = "bca";
        B64String initializationVector = B64String.from("abc".repeat(2));

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        Exception exception = assertThrows(InvalidAlgorithmParameterException.class, () -> {
            crypto.encrypt(plainText, initializationVector);
        });

        assertEquals("Invalid nonce", exception.getMessage());
    }

    @Test
    void encrypt__aes_256_initialization_vector_too_long() throws InvalidKeyException, InvalidAlgorithmParameterException {
        B64String secret = B64String.from("w".repeat(32));
        String plainText = "bca";
        B64String initializationVector = B64String.from("abc".repeat(50));

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        Exception exception = assertThrows(InvalidAlgorithmParameterException.class, () -> {
            crypto.encrypt(plainText, initializationVector);
        });

        assertEquals("Invalid nonce", exception.getMessage());
    }

    @Test
    void decrypt__aes_256() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        B64String secret = B64String.from("w".repeat(32));
        String expectedPlainText = "bca";
        B64String cipherText = B64String.fromBase64String("1qKKtEpM2ppl4wWrJxJo0MiFdw==");
        B64String initializationVector = B64String.from("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv crypto = AesGcmSiv.create(secret);
        String plainText = crypto.decrypt(cipherText, initializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void encrypt__aes_128() throws InvalidKeyException, InvalidAlgorithmParameterException {
        B64String secret = B64String.from("a".repeat(16));
        String plainText = "bca";
        String expectedCipherText = "yGcKhuWbewS+R4tlegECshiTSQ==";
        B64String initializationVector = B64String.from("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        B64String cipherText = crypto.encrypt(plainText, initializationVector);

        assertEquals(expectedCipherText, cipherText.toString());
    }

    @Test
    void decrypt__aes_128() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        B64String secret = B64String.from("a".repeat(16));
        String expectedPlainText = "bca";
        B64String b64CipherText = B64String.fromBase64String("yGcKhuWbewS+R4tlegECshiTSQ==");
        B64String initializationVector = B64String.from("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        String plainText = crypto.decrypt(b64CipherText, initializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void decrypt__aes_128_wrong_cipher_text() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        B64String secret = B64String.from("a".repeat(16));
        B64String cipherText = B64String.from("hello world, this is base 64 encoded");
        B64String initializationVector = B64String.from("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        Exception exception = assertThrows(AEADBadTagException.class, () -> {
            crypto.decrypt(cipherText, initializationVector);
        });

        assertEquals("mac check failed", exception.getMessage());
    }

    @Test
    void decrypt__aes_256_wrong_cipher_text() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        B64String secret = B64String.from("a".repeat(32));
        B64String cipherText = B64String.from("hello world, this is base 64 encoded");
        B64String initializationVector = B64String.from("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        Exception exception = assertThrows(AEADBadTagException.class, () -> {
            crypto.decrypt(cipherText, initializationVector);
        });

        assertEquals("mac check failed", exception.getMessage());
    }

    
    @Test
    void encrypt__aes_128_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        B64String secret = B64String.from("a".repeat(16));
        String plainText = LONG_TEXT;
        B64String expectedCipherText = B64String.fromBase64String("28/RdEWgYbpbiraiWcSo58+8sfCRRQpSoZiiFqNsYN8tLLVE6AXeQjxh4zazK65G7T0dmFnqrbyx6aRUB+7I6guFXMxqjRij9HdRkae4OalWZVNtCs2+mjBBMNOB5Ke2bgIcYDZbDMRWceBtJnE5PKg7vxrNFgR+8uFw9ejbRVxzGTbkyNeh48QVT9Knk7LpmqQ/eHFTvsvnD0M=");
        B64String initializationVector = B64String.from("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        B64String cipherText = crypto.encrypt(plainText, initializationVector);

        assertEquals(expectedCipherText, cipherText);
    }

    @Test
    void encrypt__aes_256_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        B64String secret = B64String.from("a".repeat(32));
        String plainText = LONG_TEXT;
        B64String expectedCipherText = B64String.fromBase64String("E2RrqhXtKG39okWxxvw3d4NQ+DXr2+Qa78JvdpHS4+FOckRECTkjoX2JfZNKHP3on0sDO1q8uTc+BY9QJkMK+MsWzp8YT4SR0UxWo7uy5SSPMXOLLcQg0vzTOTdgo00vPQy34vogNYO1V/TTzOzzP6Ng0kT9TDsYUWu+v0y3uZw/ujl2X8bP8Nfrp2ZRMrgfpj7NbjQQd8hD5AY=");
        B64String initializationVector = B64String.from("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        B64String cipherText = crypto.encrypt(plainText, initializationVector);

        assertEquals(expectedCipherText, cipherText);
    }

    @Test
    void decrypt__aes_128_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        B64String secret = B64String.from("a".repeat(16));
        String expectedPlainText = LONG_TEXT;
        B64String cipherText = B64String.fromBase64String("28/RdEWgYbpbiraiWcSo58+8sfCRRQpSoZiiFqNsYN8tLLVE6AXeQjxh4zazK65G7T0dmFnqrbyx6aRUB+7I6guFXMxqjRij9HdRkae4OalWZVNtCs2+mjBBMNOB5Ke2bgIcYDZbDMRWceBtJnE5PKg7vxrNFgR+8uFw9ejbRVxzGTbkyNeh48QVT9Knk7LpmqQ/eHFTvsvnD0M=");
        B64String initializationVector = B64String.from("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        String plainText = crypto.decrypt(cipherText, initializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    void decrypt__aes_256_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        B64String secret = B64String.from("a".repeat(32));
        String expectedPlainText = LONG_TEXT;
        B64String cipherText = B64String.fromBase64String("E2RrqhXtKG39okWxxvw3d4NQ+DXr2+Qa78JvdpHS4+FOckRECTkjoX2JfZNKHP3on0sDO1q8uTc+BY9QJkMK+MsWzp8YT4SR0UxWo7uy5SSPMXOLLcQg0vzTOTdgo00vPQy34vogNYO1V/TTzOzzP6Ng0kT9TDsYUWu+v0y3uZw/ujl2X8bP8Nfrp2ZRMrgfpj7NbjQQd8hD5AY=");
        B64String initializationVector = B64String.from("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv crypto = AesGcmSiv.create(secret);

        String plainText = crypto.decrypt(cipherText, initializationVector);

        assertEquals(expectedPlainText, plainText);
    }
    
    @Test
    void getCiphers_aes_256() throws InvalidKeyException {
        B64String secret = B64String.from("a".repeat(32));
        AesGcmSiv crypto = AesGcmSiv.create(secret);
        
        assertEquals(PersistenceCipherType.AES_256_GCM_SIV, crypto.getCipher());
    }
    
    @Test
    void getCiphers_aes_128() throws InvalidKeyException {
        B64String secret = B64String.from("a".repeat(16));
        AesGcmSiv crypto = AesGcmSiv.create(secret);
        
        assertEquals(PersistenceCipherType.AES_128_GCM_SIV, crypto.getCipher());
    }
}
