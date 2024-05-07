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
    void generate_new_initialization_vector() throws InvalidKeyException {
        /* prepare */
        BinaryString secret = new Base64String("a".repeat(32));
        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        BinaryString initializationVector = cipher.generateNewInitializationVector();

        /* test */
        assertEquals(AesGcmSiv.IV_LENGTH_IN_BYTES, initializationVector.getBytes().length);
    }

    @Test
    void create_secret_32_bytes() throws InvalidKeyException {
        /* prepare */
        BinaryString secret = new Base64String("a".repeat(32));

        /* execute */
        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* test */
        assertNotNull(cipher);
        assertEquals(PersistenceCipherType.AES_GCM_SIV_256, cipher.getCipherType());
    }

    @Test
    void create_secret_16_bytes() throws InvalidKeyException {
        /* prepare */
        BinaryString secret = new Base64String("a".repeat(16));

        /* execute */
        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* test */
        assertNotNull(cipher);
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, cipher.getCipherType());
    }

    @Test
    void create_secret_secret_6_bytes_invalid() {
        /* prepare */
        BinaryString secret = new Base64String("abcdef");

        /* execute + test */
        assertThrows(InvalidKeyException.class, () -> {
            AesGcmSiv.create(secret);
        });
    }

    @Test
    void create_secret_secret_31_bytes_invalid() {
        /* prepare */
        BinaryString secret = new Base64String("a".repeat(31));

        /* execute + test */
        assertThrows(InvalidKeyException.class, () -> {
            AesGcmSiv.create(secret);
        });
    }

    @Test
    void encrypt__aes_256() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {

        /* prepare */
        BinaryString secret = new Base64String("w".repeat(32));
        String plaintext = "bca";
        String expectedCiphertext = "1qKKtEpM2ppl4wWrJxJo0MiFdw==";
        BinaryString initializationVector = new Base64String("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        BinaryString ciphertext = cipher.encrypt(plaintext, initializationVector);

        /* test */
        assertEquals(expectedCiphertext, ciphertext.toString());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_256, cipher.getCipherType());
    }

    @Test
    void encrypt__aes_256_initialization_vector_too_short() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        /* prepare */
        BinaryString secret = new Base64String("w".repeat(32));
        String plaintext = "bca";
        BinaryString initializationVector = new Base64String("abc".repeat(2));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        Exception exception = assertThrows(InvalidAlgorithmParameterException.class, () -> {
            cipher.encrypt(plaintext, initializationVector);
        });

        /* test */
        assertEquals("Invalid nonce", exception.getMessage());
    }

    @Test
    void encrypt__aes_256_initialization_vector_too_long() throws InvalidKeyException, InvalidAlgorithmParameterException {

        /* prepare */
        BinaryString secret = new Base64String("w".repeat(32));
        String plaintext = "bca";
        BinaryString initializationVector = new Base64String("abc".repeat(50));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        Exception exception = assertThrows(InvalidAlgorithmParameterException.class, () -> {
            cipher.encrypt(plaintext, initializationVector);
        });

        /* test */
        assertEquals("Invalid nonce", exception.getMessage());
    }

    @Test
    void decrypt__aes_256() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        BinaryString secret = new Base64String("w".repeat(32));
        String expectedPlaintext = "bca";
        BinaryString ciphertext = BinaryStringFactory.createFromBase64("1qKKtEpM2ppl4wWrJxJo0MiFdw==", BinaryStringEncodingType.BASE64);
        BinaryString initializationVector = new Base64String("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        String plaintext = cipher.decrypt(ciphertext, initializationVector);

        /* test */
        assertEquals(expectedPlaintext, plaintext);
    }

    @Test
    void encrypt__aes_128() throws InvalidKeyException, InvalidAlgorithmParameterException {
        /* prepare */
        BinaryString secret = new Base64String("a".repeat(16));
        String plaintext = "bca";
        String expectedCiphertext = "yGcKhuWbewS+R4tlegECshiTSQ==";
        BinaryString initializationVector = new Base64String("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        BinaryString ciphertext = cipher.encrypt(plaintext, initializationVector);

        /* test */
        assertEquals(expectedCiphertext, ciphertext.toString());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, cipher.getCipherType());
    }

    @Test
    void encrypt__aes_256_hex_format_and_emojis() throws InvalidKeyException, InvalidAlgorithmParameterException {
        /* prepare */
        BinaryString secret = new HexString("🥦🥕🥔🫘🥒🫑🌽🍆");
        String plaintext = "Hello 👋, welcome to 🌐.";
        String expectedCiphertext = "d09be77ddd8dbac86b69b0f5f554faef740555ac93f12aedfdf62700e4ea3016e03dacc105f32f114791d8e6";
        BinaryString initializationVector = new Base64String("🧅".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES / 4));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        BinaryString ciphertext = cipher.encrypt(plaintext, initializationVector, BinaryStringEncodingType.HEX);

        /* test */
        assertEquals(expectedCiphertext, ciphertext.toString());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_256, cipher.getCipherType());
    }

    @Test
    void encrypt__aes_128_base64_format_and_emojis() throws InvalidKeyException, InvalidAlgorithmParameterException {
        /* prepare */
        BinaryString secret = new Base64String("🍐🍌🍓🍉");

        String plaintext = "Hello 👋, welcome to 🌐.";
        String expectedCiphertext = "Qu7ICJBGMw9dAPPBWx86e5bjOq3YKC+x25n/YkluWZAGdSna08tKaE78pMk=";
        BinaryString initializationVector = new Base64String("🧅".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES / 4));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        BinaryString ciphertext = cipher.encrypt(plaintext, initializationVector, BinaryStringEncodingType.BASE64);

        /* test */
        assertEquals(expectedCiphertext, ciphertext.toString());
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, cipher.getCipherType());
    }

    @Test
    void decrypt__aes_128() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        BinaryString secret = new Base64String("a".repeat(16));
        String expectedPlaintext = "bca";
        BinaryString cipherText = BinaryStringFactory.createFromBase64("yGcKhuWbewS+R4tlegECshiTSQ==", BinaryStringEncodingType.BASE64);
        BinaryString initializationVector = new Base64String("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        String plaintext = cipher.decrypt(cipherText, initializationVector);

        /* test */
        assertEquals(expectedPlaintext, plaintext);
    }

    @Test
    void decrypt__aes_128_wrong_cipher_text() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        /* prepare */
        BinaryString secret = new Base64String("a".repeat(16));
        BinaryString ciphertext = new Base64String("hello world, this is base 64 encoded");
        BinaryString initializationVector = new Base64String("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        Exception exception = assertThrows(AEADBadTagException.class, () -> {
            cipher.decrypt(ciphertext, initializationVector);
        });

        /* test */
        assertEquals("mac check failed", exception.getMessage());
    }

    @Test
    void decrypt__aes_256_wrong_cipher_text() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {

        /* prepare */
        BinaryString secret = new Base64String("a".repeat(32));
        BinaryString ciphertext = new Base64String("hello world, this is base 64 encoded");
        BinaryString initializationVector = new Base64String("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        Exception exception = assertThrows(AEADBadTagException.class, () -> {
            cipher.decrypt(ciphertext, initializationVector);
        });

        /* test */
        assertEquals("mac check failed", exception.getMessage());
    }

    @Test
    void encrypt__aes_128_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        /* prepare */
        BinaryString secret = new Base64String("a".repeat(16));
        String plaintext = LONG_TEXT;
        BinaryString expectedCiphertext = BinaryStringFactory.createFromBase64(
                "28/RdEWgYbpbiraiWcSo58+8sfCRRQpSoZiiFqNsYN8tLLVE6AXeQjxh4zazK65G7T0dmFnqrbyx6aRUB+7I6guFXMxqjRij9HdRkae4OalWZVNtCs2+mjBBMNOB5Ke2bgIcYDZbDMRWceBtJnE5PKg7vxrNFgR+8uFw9ejbRVxzGTbkyNeh48QVT9Knk7LpmqQ/eHFTvsvnD0M=",
                BinaryStringEncodingType.BASE64);
        BinaryString initializationVector = new Base64String("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        BinaryString ciphertext = cipher.encrypt(plaintext, initializationVector);

        /* test */
        assertEquals(expectedCiphertext, ciphertext);
    }

    @Test
    void encrypt__aes_256_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        /* prepare */
        BinaryString secret = new Base64String("a".repeat(32));
        String plaintext = LONG_TEXT;
        BinaryString expectedCiphertext = BinaryStringFactory.createFromBase64(
                "E2RrqhXtKG39okWxxvw3d4NQ+DXr2+Qa78JvdpHS4+FOckRECTkjoX2JfZNKHP3on0sDO1q8uTc+BY9QJkMK+MsWzp8YT4SR0UxWo7uy5SSPMXOLLcQg0vzTOTdgo00vPQy34vogNYO1V/TTzOzzP6Ng0kT9TDsYUWu+v0y3uZw/ujl2X8bP8Nfrp2ZRMrgfpj7NbjQQd8hD5AY=",
                BinaryStringEncodingType.BASE64);
        BinaryString initializationVector = new Base64String("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        BinaryString ciphertext = cipher.encrypt(plaintext, initializationVector);

        /* test */
        assertEquals(expectedCiphertext, ciphertext);
    }

    @Test
    void decrypt__aes_128_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        /* prepare */
        BinaryString secret = new Base64String("a".repeat(16));
        String expectedPlaintext = LONG_TEXT;
        BinaryString ciphertext = BinaryStringFactory.createFromBase64(
                "28/RdEWgYbpbiraiWcSo58+8sfCRRQpSoZiiFqNsYN8tLLVE6AXeQjxh4zazK65G7T0dmFnqrbyx6aRUB+7I6guFXMxqjRij9HdRkae4OalWZVNtCs2+mjBBMNOB5Ke2bgIcYDZbDMRWceBtJnE5PKg7vxrNFgR+8uFw9ejbRVxzGTbkyNeh48QVT9Knk7LpmqQ/eHFTvsvnD0M=",
                BinaryStringEncodingType.BASE64);
        BinaryString initializationVector = new Base64String("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        String plaintext = cipher.decrypt(ciphertext, initializationVector);

        /* test */
        assertEquals(expectedPlaintext, plaintext);
    }

    @Test
    void decrypt__aes_256_long_text_with_emojis() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        /* prepare */
        BinaryString secret = new Base64String("a".repeat(32));
        String expectedPlaintext = LONG_TEXT;
        BinaryString ciphertext = BinaryStringFactory.createFromBase64(
                "E2RrqhXtKG39okWxxvw3d4NQ+DXr2+Qa78JvdpHS4+FOckRECTkjoX2JfZNKHP3on0sDO1q8uTc+BY9QJkMK+MsWzp8YT4SR0UxWo7uy5SSPMXOLLcQg0vzTOTdgo00vPQy34vogNYO1V/TTzOzzP6Ng0kT9TDsYUWu+v0y3uZw/ujl2X8bP8Nfrp2ZRMrgfpj7NbjQQd8hD5AY=",
                BinaryStringEncodingType.BASE64);
        BinaryString initializationVector = new Base64String("i".repeat(AesGcmSiv.IV_LENGTH_IN_BYTES));

        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* execute */
        String plaintext = cipher.decrypt(ciphertext, initializationVector);

        /* test */
        assertEquals(expectedPlaintext, plaintext);
    }

    @Test
    void getCipherType_aes_256() throws InvalidKeyException {
        /* prepare */
        BinaryString secret = new Base64String("a".repeat(32));

        /* execute */
        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* test */
        assertEquals(PersistenceCipherType.AES_GCM_SIV_256, cipher.getCipherType());
    }

    @Test
    void getCiphersType_aes_128() throws InvalidKeyException {
        /* prepare */
        BinaryString secret = new Base64String("a".repeat(16));

        /* execute */
        AesGcmSiv cipher = AesGcmSiv.create(secret);

        /* test */
        assertEquals(PersistenceCipherType.AES_GCM_SIV_128, cipher.getCipherType());
    }
}
