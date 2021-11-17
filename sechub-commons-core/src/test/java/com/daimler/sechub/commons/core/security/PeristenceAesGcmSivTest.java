package com.daimler.sechub.commons.core.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PeristenceAesGcmSivTest {
    @Test
    public void generate_new_initialization_vector() {
        String b64InitializationVector = PeristenceAesGcmSiv.generateNewInitializationVector();
        byte[] initializationVector = Base64.getDecoder().decode(b64InitializationVector);

        assertEquals(PeristenceAesGcmSiv.IV_LENGTH_IN_BYTES, initializationVector.length);
    }

    @Test
    public void init_secret_32_bytes() throws InvalidKeyException {
        String secret = Base64.getEncoder().encodeToString(repeatString("a", 32).getBytes());
        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);

        assertNotNull(crypto);
    }

    @Test
    public void init_secret_16_bytes() throws InvalidKeyException {
        String secret = Base64.getEncoder().encodeToString(repeatString("a", 16).getBytes());
        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);

        assertNotNull(crypto);
    }

    @Test
    public void init_secret_secret_6_bytes_invalid() {
        String secret = Base64.getEncoder().encodeToString("abcdef".getBytes());

        assertThrows(InvalidKeyException.class, () -> {
            PeristenceAesGcmSiv.init(secret);
        });
    }

    @Test
    public void init_secret_secret_31_bytes_invalid() {
        String secret = Base64.getEncoder().encodeToString(repeatString("a", 31).getBytes());

        assertThrows(InvalidKeyException.class, () -> {
            PeristenceAesGcmSiv.init(secret);
        });
    }

    @Test
    public void encrypt__aes_256() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        String secret = Base64.getEncoder().encodeToString(repeatString("w", 32).getBytes());
        String plainText = "bca";
        String expectedCipherText = "1qKKtEpM2ppl4wWrJxJo0MiFdw==";
        String b64InitializationVector = repeatStringBase64Encoded("i", PeristenceAesGcmSiv.IV_LENGTH_IN_BYTES);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);
        String cipherText = crypto.encrypt(plainText, b64InitializationVector);

        assertEquals(expectedCipherText, cipherText);
    }
    
    @Test
    public void encrypt__aes_256_initialization_vector_too_short() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        String secret = Base64.getEncoder().encodeToString(repeatString("w", 32).getBytes());
        String plainText = "bca";
        String b64InitializationVector = repeatStringBase64Encoded("abc", 2);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);
        
        Exception exception = assertThrows(InvalidAlgorithmParameterException.class, () -> {
            crypto.encrypt(plainText, b64InitializationVector);
        });

        assertEquals("Invalid nonce", exception.getMessage());
    }
    
    @Test
    public void encrypt__aes_256_initialization_vector_too_long() throws InvalidKeyException, InvalidAlgorithmParameterException {
        String secret = Base64.getEncoder().encodeToString(repeatString("w", 32).getBytes());
        String plainText = "bca";
        String b64InitializationVector = repeatStringBase64Encoded("abc", 50);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);
        
        Exception exception = assertThrows(InvalidAlgorithmParameterException.class, () -> {
            crypto.encrypt(plainText, b64InitializationVector);
        });

        assertEquals("Invalid nonce", exception.getMessage());
    }

    @Test
    public void decrypt__aes_256() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        String secret = Base64.getEncoder().encodeToString(repeatString("w", 32).getBytes());
        String expectedPlainText = "bca";
        String cipherText = "1qKKtEpM2ppl4wWrJxJo0MiFdw==";
        String b64InitializationVector = repeatStringBase64Encoded("i", PeristenceAesGcmSiv.IV_LENGTH_IN_BYTES);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);
        String plainText = crypto.decrypt(cipherText, b64InitializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    @Test
    public void encrypt__aes_128() throws InvalidKeyException, InvalidAlgorithmParameterException {
        String secret = repeatStringBase64Encoded("a", 16);
        String plainText = "bca";
        String expectedCipherText = "yGcKhuWbewS+R4tlegECshiTSQ==";
        String b64InitializationVector = repeatStringBase64Encoded("i", PeristenceAesGcmSiv.IV_LENGTH_IN_BYTES);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);

        String cipherText = crypto.encrypt(plainText, b64InitializationVector);

        assertEquals(expectedCipherText, cipherText);
    }

    @Test
    public void decrypt__aes_128() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 16);
        String expectedPlainText = "bca";
        String b64CipherText = "yGcKhuWbewS+R4tlegECshiTSQ==";
        String b64InitializationVector = repeatStringBase64Encoded("i", PeristenceAesGcmSiv.IV_LENGTH_IN_BYTES);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);

        String plainText = crypto.decrypt(b64CipherText, b64InitializationVector);

        assertEquals(expectedPlainText, plainText);
    }
    
    @Test
    public void decrypt__aes_128_wrong_cipher_text() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 16);
        String b64CipherText = Base64.getEncoder().encodeToString("hello world, this is base 64 encoded".getBytes());
        String b64InitializationVector = repeatStringBase64Encoded("i", PeristenceAesGcmSiv.IV_LENGTH_IN_BYTES);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);

        Exception exception = assertThrows(AEADBadTagException.class, () -> {
            crypto.decrypt(b64CipherText, b64InitializationVector);
        });
        
        assertEquals("mac check failed", exception.getMessage());
    }

    @Test
    public void encrypt__aes_128_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 16);
        String plainText = "Hello world, this is long text with different emojis. Today, I had for breakfast two 🥐, 1 🥑 and some 🥪. That made me happy ☺️!";
        String expectedCipherText = "28/RdEWgYbpbiraiWcSo58+8sfCRRQpSoZiiFqNsYN8tLLVE6AXeQjxh4zazK65G7T0dmFnqrbyx6aRUB+7I6guFXMxqjRij9HdRkae4OalWZVNtCs2+mjBBMNOB5Ke2bgIcYDZbDMRWceBtJnE5PKg7vxrNFgR+8uFw9ejbRVxzGTbkyNeh48QVT9Knk7LpmqQ/eHFTvsvnD0M=";
        String b64InitializationVector = repeatStringBase64Encoded("i", PeristenceAesGcmSiv.IV_LENGTH_IN_BYTES);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);

        String cipherText = crypto.encrypt(plainText, b64InitializationVector);

        assertEquals(expectedCipherText, cipherText);
    }
    
    @Test
    public void encrypt__aes_256_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 32);
        String plainText = "Hello world, this is long text with different emojis. Today, I had for breakfast two 🥐, 1 🥑 and some 🥪. That made me happy ☺️!";
        String expectedCipherText = "E2RrqhXtKG39okWxxvw3d4NQ+DXr2+Qa78JvdpHS4+FOckRECTkjoX2JfZNKHP3on0sDO1q8uTc+BY9QJkMK+MsWzp8YT4SR0UxWo7uy5SSPMXOLLcQg0vzTOTdgo00vPQy34vogNYO1V/TTzOzzP6Ng0kT9TDsYUWu+v0y3uZw/ujl2X8bP8Nfrp2ZRMrgfpj7NbjQQd8hD5AY=";
        String b64InitializationVector = repeatStringBase64Encoded("i", PeristenceAesGcmSiv.IV_LENGTH_IN_BYTES);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);

        String cipherText = crypto.encrypt(plainText, b64InitializationVector);

        assertEquals(expectedCipherText, cipherText);
    }
    
    @Test
    public void decrypt__aes_128_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 16);
        String expectedPlainText = "Hello world, this is long text with different emojis. Today, I had for breakfast two 🥐, 1 🥑 and some 🥪. That made me happy ☺️!";
        String cipherText = "28/RdEWgYbpbiraiWcSo58+8sfCRRQpSoZiiFqNsYN8tLLVE6AXeQjxh4zazK65G7T0dmFnqrbyx6aRUB+7I6guFXMxqjRij9HdRkae4OalWZVNtCs2+mjBBMNOB5Ke2bgIcYDZbDMRWceBtJnE5PKg7vxrNFgR+8uFw9ejbRVxzGTbkyNeh48QVT9Knk7LpmqQ/eHFTvsvnD0M=";
        String b64InitializationVector = repeatStringBase64Encoded("i", PeristenceAesGcmSiv.IV_LENGTH_IN_BYTES);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);

        String plainText = crypto.decrypt(cipherText, b64InitializationVector);

        assertEquals(expectedPlainText, plainText);
    }
    
    @Test
    public void decrypt__aes_256_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String secret = repeatStringBase64Encoded("a", 32);
        String expectedPlainText = "Hello world, this is long text with different emojis. Today, I had for breakfast two 🥐, 1 🥑 and some 🥪. That made me happy ☺️!";
        String cipherText = "E2RrqhXtKG39okWxxvw3d4NQ+DXr2+Qa78JvdpHS4+FOckRECTkjoX2JfZNKHP3on0sDO1q8uTc+BY9QJkMK+MsWzp8YT4SR0UxWo7uy5SSPMXOLLcQg0vzTOTdgo00vPQy34vogNYO1V/TTzOzzP6Ng0kT9TDsYUWu+v0y3uZw/ujl2X8bP8Nfrp2ZRMrgfpj7NbjQQd8hD5AY=";
        String b64InitializationVector = repeatStringBase64Encoded("i", PeristenceAesGcmSiv.IV_LENGTH_IN_BYTES);

        PeristenceAesGcmSiv crypto = PeristenceAesGcmSiv.init(secret);

        String plainText = crypto.decrypt(cipherText, b64InitializationVector);

        assertEquals(expectedPlainText, plainText);
    }

    private String repeatStringBase64Encoded(String sequence, int times) {
        return Base64.getEncoder().encodeToString(repeatString(sequence, times).getBytes());
    }

    private String repeatString(String sequence, int times) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < times; i++) {
            sb.append(sequence);
        }

        return sb.toString();
    }
}
