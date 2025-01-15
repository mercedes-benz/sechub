// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.mercedesbenz.sechub.commons.model.login.EncodingType;
import com.mercedesbenz.sechub.commons.model.login.TOTPHashAlgorithm;
import com.mercedesbenz.sechub.commons.model.login.WebLoginTOTPConfiguration;

class TOTPGeneratorTest {

    @Test
    void totp_config_being_null_throws_exception() {
        /* execute + test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new TOTPGenerator(null));

        assertEquals("The TOTP configuration must be configured to generate TOTP values!", exception.getMessage());
    }

    @Test
    void totp_seed_being_null_throws_exception() {
        /* prepare */
        WebLoginTOTPConfiguration webLoginTOTPConfiguration = new WebLoginTOTPConfiguration();
        webLoginTOTPConfiguration.setSeed(null);

        /* execute + test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new TOTPGenerator(webLoginTOTPConfiguration));

        assertEquals("The TOTP configuration seed must be configured to generate TOTP values!", exception.getMessage());
    }

    @Test
    void totp_hash_algorithm_being_null_throws_exception() {
        /* prepare */
        WebLoginTOTPConfiguration webLoginTOTPConfiguration = new WebLoginTOTPConfiguration();
        webLoginTOTPConfiguration.setSeed("test-seed");
        webLoginTOTPConfiguration.setHashAlgorithm(null);

        /* execute + test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new TOTPGenerator(webLoginTOTPConfiguration));

        assertEquals("The TOTP configuration hash algorithm must be configured to generate TOTP values!", exception.getMessage());
    }

    @Test
    void totp_encoding_type_being_null_throws_exception() {
        /* prepare */
        WebLoginTOTPConfiguration webLoginTOTPConfiguration = new WebLoginTOTPConfiguration();
        webLoginTOTPConfiguration.setSeed("test-seed");
        webLoginTOTPConfiguration.setEncodingType(null);

        /* execute + test */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new TOTPGenerator(webLoginTOTPConfiguration));

        assertEquals("The TOTP configuration encoding type must be configured to generate TOTP values!", exception.getMessage());
    }

    @Test
    void generate_the_excpected_otp_with_default_config() throws DecoderException {
        /* prepare */
        String seed = "NFQDO2DXCNAHULZU";
        long timeMillis = 1724650799055L;
        String expectedToken = "950308";
        WebLoginTOTPConfiguration totpConfig = new WebLoginTOTPConfiguration();
        totpConfig.setSeed(seed);

        TOTPGenerator totpGenerator = new TOTPGenerator(totpConfig);

        /* execute */
        String generatedToken = totpGenerator.generateTOTP(timeMillis);

        /* test */
        assertEquals(expectedToken, generatedToken);
    }

    @ParameterizedTest
    @ArgumentsSource(RFC6238TOTPArgumentsProvider.class)
    void rfc_6238_test_data_generate_the_excpected_otp(String seed, long timeInMillis, TOTPHashAlgorithm algorithm, int totpLength,
            int totpValidityTimeInSeconds, String expectedToken) throws DecoderException {
        /* prepare */
        WebLoginTOTPConfiguration totpConfig = new WebLoginTOTPConfiguration();
        totpConfig.setSeed(seed);
        totpConfig.setEncodingType(EncodingType.HEX);
        totpConfig.setTokenLength(totpLength);
        totpConfig.setHashAlgorithm(algorithm);
        totpConfig.setValidityInSeconds(totpValidityTimeInSeconds);

        TOTPGenerator totpGenerator = new TOTPGenerator(totpConfig);

        /* execute */
        String generatedToken = totpGenerator.generateTOTP(timeInMillis);

        /* test */
        assertEquals(expectedToken, generatedToken);
    }

    /**
     * Test data from: https://datatracker.ietf.org/doc/html/rfc6238#appendix-B
     */
    private static class RFC6238TOTPArgumentsProvider implements ArgumentsProvider {
        private static final String SHA1_SEED = "3132333435363738393031323334353637383930";
        private static final String SHA256_SEED = "3132333435363738393031323334353637383930313233343536373839303132";
        private static final String SHA512_SEED = "31323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334";

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            /* @formatter:off */
            return Stream.of(
                    Arguments.of(SHA1_SEED,   59000L,          TOTPHashAlgorithm.HMAC_SHA1,   8, 30, "94287082"),
                    Arguments.of(SHA1_SEED,   1111111109000L,  TOTPHashAlgorithm.HMAC_SHA1,   8, 30, "07081804"),
                    Arguments.of(SHA1_SEED,   1111111111000L,  TOTPHashAlgorithm.HMAC_SHA1,   8, 30, "14050471"),
                    Arguments.of(SHA1_SEED,   1234567890000L,  TOTPHashAlgorithm.HMAC_SHA1,   8, 30, "89005924"),
                    Arguments.of(SHA1_SEED,   2000000000000L,  TOTPHashAlgorithm.HMAC_SHA1,   8, 30, "69279037"),
                    Arguments.of(SHA1_SEED,   20000000000000L, TOTPHashAlgorithm.HMAC_SHA1,   8, 30, "65353130"),

                    Arguments.of(SHA256_SEED, 59000L,          TOTPHashAlgorithm.HMAC_SHA256, 8, 30, "46119246"),
                    Arguments.of(SHA256_SEED, 1111111109000L,  TOTPHashAlgorithm.HMAC_SHA256, 8, 30, "68084774"),
                    Arguments.of(SHA256_SEED, 1111111111000L,  TOTPHashAlgorithm.HMAC_SHA256, 8, 30, "67062674"),
                    Arguments.of(SHA256_SEED, 1234567890000L,  TOTPHashAlgorithm.HMAC_SHA256, 8, 30, "91819424"),
                    Arguments.of(SHA256_SEED, 2000000000000L,  TOTPHashAlgorithm.HMAC_SHA256, 8, 30, "90698825"),
                    Arguments.of(SHA256_SEED, 20000000000000L, TOTPHashAlgorithm.HMAC_SHA256, 8, 30, "77737706"),

                    Arguments.of(SHA512_SEED, 59000L,          TOTPHashAlgorithm.HMAC_SHA512, 8, 30, "90693936"),
                    Arguments.of(SHA512_SEED, 1111111109000L,  TOTPHashAlgorithm.HMAC_SHA512, 8, 30, "25091201"),
                    Arguments.of(SHA512_SEED, 1111111111000L,  TOTPHashAlgorithm.HMAC_SHA512, 8, 30, "99943326"),
                    Arguments.of(SHA512_SEED, 1234567890000L,  TOTPHashAlgorithm.HMAC_SHA512, 8, 30, "93441116"),
                    Arguments.of(SHA512_SEED, 2000000000000L,  TOTPHashAlgorithm.HMAC_SHA512, 8, 30, "38618901"),
                    Arguments.of(SHA512_SEED, 20000000000000L, TOTPHashAlgorithm.HMAC_SHA512, 8, 30, "47863826")
            );
            /* @formatter:on */
        }
    }
}
