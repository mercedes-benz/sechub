package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

class Sha256ChecksumValidationImplTest {

    private Sha256ChecksumValidationImpl toTest;

    @BeforeEach
    void beforeEach() {
        toTest = new Sha256ChecksumValidationImpl();
    }

    @ParameterizedTest
    @CsvSource({
    /* @formatter:off */
        "c590b3c924c35c2f241746522284e4709df490d73a38aaa7d6de4ed1eac2f546",
        "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
        "1111111111111111111111111111111111111111111111111111111111111111",
        "2222222222222222222222222222222222222222222222222222222222222222",
        "012345679012345679a12345679b12345679c12345679d12345679e12345679f",
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        /* @formatter:on */
    })
    void valid_sha256_is_valid(String sha256) {
        /* execute */
        ValidationResult result = toTest.validate(sha256);

        /* test */
        if (!result.isValid()) {
            fail("Invalid sha256 must be recognized correctly - but wasn't!\nMessage was:" + result.getErrorDescription());
        }
    }

    @ParameterizedTest
    @CsvSource({
    /* @formatter:off */
        "c590b3c924c35c2f241746522284e4709df490d73a38aaa7d6de4ed1eac2f5461",
        "012345679012345679a12345679b12345679c12345679d12345679e12345679f2",
        "22222222222222222222222222222222222222222222222222222222222222223"
        /* @formatter:on */
    })
    void sha256_string_being_too_long_are_not_valid(String sha256) {
        /* execute */
        ValidationResult result = toTest.validate(sha256);

        /* test */
        if (result.isValid()) {
            fail("Invalid sha256 must be recognized correctly - but wasn't!\nMessage was:" + result.getErrorDescription());
        }
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @CsvSource({
    /* @formatter:off */
        "590b3c924c35c2f241746522284e4709df490d73a38aaa7d6de4ed1eac2f54",
        "123456790123456790123456790123456790123456790123456790123456790",
        "22222222222222222222222222222222222222222222222222222222222222"
        /* @formatter:on */
    })
    void sha256_string_being_too_short_are_not_valid(String sha256) {
        /* execute */
        ValidationResult result = toTest.validate(sha256);

        /* test */
        if (result.isValid()) {
            fail("Invalid sha256 must be recognized correctly - but wasn't!\nMessage was:" + result.getErrorDescription());
        }
    }

    @ParameterizedTest
    @CsvSource({
    /* @formatter:off */
        "-----------------!-------------------------------------------546",
        "g590b3c924c35c2f241746522284e4709df490d73a38aaa7d6de4ed1eac2f546",
        "g590b3c924c35c2f241746522284e4709df490d73a38aaa7d6de4ed1eac2f546",
        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
        /* @formatter:on */
    })
    void sha256_string_with_parts_not_being_hex_characters_are_not_valid(String sha256) {
        /* execute */
        ValidationResult result = toTest.validate(sha256);

        /* test */
        if (result.isValid()) {
            fail("Valid sha256 must be recognized correctly - but wasn't!\nMessage was:" + result.getErrorDescription());
        }
    }

    @ParameterizedTest
    @CsvSource({
    /* @formatter:off */
        "apple",
        "banana",
        "'lemon, lime'",
        "a strawberry is inside strawberries..."
        /* @formatter:on */
    })
    void sha256_digest_generated_output_for_content_is_valid(String content) throws Exception {
        /* prepare */
        String sha256 = createSha256(content);

        /* execute */
        ValidationResult result = toTest.validate(sha256);

        /* test */
        if (!result.isValid()) {
            fail("generated sha:" + sha256 + "\nlength:" + sha256.length() + " was not valid?!?!\nMessage was:" + result.getErrorDescription());
        }
    }

    private String createSha256(String contentAsString) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        DigestInputStream stream = new DigestInputStream(new ByteArrayInputStream(contentAsString.getBytes()), md);
        StringBuilder result = new StringBuilder();
        for (byte b : stream.getMessageDigest().digest()) {
            result.append(String.format("%02x", b));
        }
        String sha256 = result.toString();
        return sha256;
    }

}
