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
    void generateToken_throws_not_acceptable_exception_basUrl_null_or_blank(String baseUrl) {
        /* prepare */
        String userId = "user1";
        String email = "user1@email";
        UserEmailChangeRecord userEmailChangeRecord = new UserEmailChangeRecord(userId, email);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.generateToken(userEmailChangeRecord, baseUrl)).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Base URL must not be null or blank!");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "  " })
    void generateToken_throws_not_acceptable_exception_userId_blank(String userId) {
        /* prepare */
        String baseUrl = "http://localhost:8080";
        String email = "user1@email";
        UserEmailChangeRecord userEmailChangeRecord = new UserEmailChangeRecord(userId, email);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.generateToken(userEmailChangeRecord, baseUrl)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User ID must not be null or blank!");
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "  " })
    void generateToken_throws_not_acceptable_exception_email_blank(String email) {
        /* prepare */
        String baseUrl = "http://localhost:8080";
        String userId = "user1";
        UserEmailChangeRecord userEmailChangeRecord = new UserEmailChangeRecord(userId, email);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.generateToken(userEmailChangeRecord, baseUrl)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email address must not be null or blank!");
    }

    @ParameterizedTest
    @CsvSource({ "user1, user1@email", "user2, user2@email", "user3, user3@email" })
    void generateToken_generates_token_from_input(String userId, String email) {
        /* prepare */
        String baseUrl = "http://localhost:8080";
        UserEmailChangeRecord userEmailChangeRecord = new UserEmailChangeRecord(userId, email);

        /* execute */
        String token = serviceToTest.generateToken(userEmailChangeRecord, baseUrl);

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
        UserEmailChangeRecord userEmailChangeRecord = serviceToTest.extractUserInfoFromToken(BASE_64_ENCODED);

        /* test */
        assertThat(userEmailChangeRecord).isNotNull();
        assertThat(userEmailChangeRecord.userId()).isEqualTo(userId);
        assertThat(userEmailChangeRecord.newEmail()).isEqualTo(email);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "  " })
    void extractUserInfoFromJWTToken_throws_not_acceptable_exception_for_null_empty_token(String invalidToken) {
        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.extractUserInfoFromToken(invalidToken)).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Token must not be null or blank!");
    }

    @Test
    void generate_token_throws_null_pointer_when_baseUrl_is_null() {
        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.generateToken(new UserEmailChangeRecord("user1", "mail"), null)).isInstanceOf(NullPointerException.class);
    }

}