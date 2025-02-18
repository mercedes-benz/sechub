// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.spring.security.AES256Encryption;

class UserEmailChangeTokenServiceTest {

    public static final String BASE_64_ENCODED = "dGhpc19pc19hbl9lbmNyeXB0ZWRfYWNjZXNzX3Rva2Vu";
    private static final String ENCRYPTED_ACCESS_TOKEN = "this_is_an_encrypted_access_token";
    private static final byte[] ENCRYPTED_ACCESS_TOKEN_BYTES = ENCRYPTED_ACCESS_TOKEN.getBytes(StandardCharsets.UTF_8);
    private UserEmailChangeTokenService serviceToTest;
    private AES256Encryption aes256Encryption;

    @BeforeEach
    void beforeEach() {
        aes256Encryption = mock(AES256Encryption.class);
        when(aes256Encryption.encrypt(anyString())).thenReturn(ENCRYPTED_ACCESS_TOKEN_BYTES);
        serviceToTest = new UserEmailChangeTokenService(aes256Encryption);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "  " })
    void generateToken_throws_not_acceptable_exception_userId_blank(String userId) {
        /* prepare */
        String email = "user1@email";
        UserEmailChangeRequest userEmailChangeRequest = new UserEmailChangeRequest(userId, email);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.generateToken(userEmailChangeRequest)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User ID must not be null or blank!");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "  " })
    void generateToken_throws_not_acceptable_exception_email_blank(String email) {
        /* prepare */
        String userId = "user1";
        UserEmailChangeRequest userEmailChangeRequest = new UserEmailChangeRequest(userId, email);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.generateToken(userEmailChangeRequest)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email address must not be null or blank!");
    }

    @ParameterizedTest
    @CsvSource({ "user1, user1@email", "user2, user2@email", "user3, user3@email" })
    void generateToken_generates_token_from_input(String userId, String email) {
        /* prepare */
        UserEmailChangeRequest userEmailChangeRequest = new UserEmailChangeRequest(userId, email);

        /* execute */
        String token = serviceToTest.generateToken(userEmailChangeRequest);

        /* test */
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token).isEqualTo(BASE_64_ENCODED);
    }

    @Test
    void expiredToken_throws_BadRequestException() {
        /* prepare */
        String expiredTimestamp = "2021-08-01T12:00:00Z";
        String userId = "user1";
        String email = "user1@email";
        UserEmailChangeToken userEmailChangeToken = new UserEmailChangeToken(userId, email, expiredTimestamp);
        String json = userEmailChangeToken.toJSON();
        when(aes256Encryption.decrypt(ENCRYPTED_ACCESS_TOKEN_BYTES)).thenReturn(json);

        /* execute */
        assertThatThrownBy(() -> serviceToTest.extractUserInfoFromToken(BASE_64_ENCODED)).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Token has expired!");

    }

    @Test
    void valid_token_reveals_information() {
        /* prepare */
        String expiredTimestamp = Clock.systemUTC().instant().toString();
        String userId = "user1";
        String email = "user1@email";
        UserEmailChangeToken expectedUserEmailChangeToken = new UserEmailChangeToken(userId, email, expiredTimestamp);
        String json = expectedUserEmailChangeToken.toJSON();
        when(aes256Encryption.decrypt(ENCRYPTED_ACCESS_TOKEN_BYTES)).thenReturn(json);

        /* execute */
        UserEmailChangeRequest userEmailChangeRequest = serviceToTest.extractUserInfoFromToken(BASE_64_ENCODED);

        /* test */
        assertThat(userEmailChangeRequest).isNotNull();
        assertThat(userEmailChangeRequest.userId()).isEqualTo(userId);
        assertThat(userEmailChangeRequest.newEmail()).isEqualTo(email);
    }
}