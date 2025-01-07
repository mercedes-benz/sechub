// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.mercedesbenz.sechub.commons.model.login.TOTPHashAlgorithm;

class TOTPGeneratorTest {

    @Test
    void secret_key_being_null_throws_exception() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> new TOTPGenerator(null, 6, TOTPHashAlgorithm.HMAC_SHA1, 30));
    }

    @Test
    void generate_the_excpected_otp_with_default_config() throws DecoderException {
        /* prepare */
        String seed = "NFQDO2DXCNAHULZU";
        byte[] seedBytes = new Base32().decode(seed);
        long timeMillis = 1724650799055L;
        String expectedToken = "950308";
        String seedDecoded = new String(seedBytes, StandardCharsets.UTF_8);

        TOTPGenerator totpGenerator = new TOTPGenerator(seedDecoded, 6, TOTPHashAlgorithm.HMAC_SHA1, 30);

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
        byte[] seedBytes = Hex.decodeHex(seed);
        String seedDecoded = new String(seedBytes, StandardCharsets.UTF_8);

        TOTPGenerator totpGenerator = new TOTPGenerator(seedDecoded, totpLength, algorithm, totpValidityTimeInSeconds);

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
