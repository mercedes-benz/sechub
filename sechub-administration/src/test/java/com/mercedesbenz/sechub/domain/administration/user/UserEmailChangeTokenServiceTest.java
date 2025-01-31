package com.mercedesbenz.sechub.domain.administration.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.spring.security.SecHubSecurityProperties;

class UserEmailChangeTokenServiceTest {

    private UserEmailChangeTokenService serviceToTest;
    private SecHubSecurityProperties secHubSecurityProperties;

    @BeforeEach
    void beforeEach() {
        secHubSecurityProperties = mock(SecHubSecurityProperties.class);
        when(secHubSecurityProperties.getEncryptionProperties()).thenReturn(mock(SecHubSecurityProperties.EncryptionProperties.class));
        when(secHubSecurityProperties.getEncryptionProperties().getSecretKey()).thenReturn("3f8a2e5b9c7d4e6f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2test");
        serviceToTest = new UserEmailChangeTokenService(secHubSecurityProperties);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", " ", "  " })
    void generateToken_throws_not_acceptable_exception_basUrlBlank(String baseUrl) {
        /* prepare */
        String userId = "user1";
        String email = "user1@email";

        UserInfo userInfo = new UserInfo(userId, email);

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.generateToken(userInfo, baseUrl)).isInstanceOf(NotAcceptableException.class)
                .hasMessageContaining("Base URL not set");
    }

    @Test
    void generateToken_throws_not_acceptable_exception_userInfoNull() {
        /* prepare */
        String baseUrl = "http://localhost:8080";

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.generateToken(null, baseUrl)).isInstanceOf(NotAcceptableException.class)
                .hasMessageContaining("User info not set");
    }

    @ParameterizedTest
    @CsvSource({ "user1, user1@email", "user2, user2@email", "user3, user3@email" })
    void generateToken_and_extractToken_reveals_same_information(String userId, String email) {
        /* prepare */
        String baseUrl = "http://localhost:8080";

        UserInfo userInfo = new UserInfo(userId, email);

        /* execute */
        String token = serviceToTest.generateToken(userInfo, baseUrl);
        UserInfo userInfoFromToken = serviceToTest.extractUserInfoFromJWTToken(token);

        /* test */
        assertNotNull(token);
        assert (userInfo.getUserId()).equals(userInfoFromToken.getUserId());
        assert (userInfo.getEmail()).equals(userInfoFromToken.getEmail());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", " ", "  " })
    void extractUserInfoFromJWTToken_throws_not_acceptable_exception_for_null_empty_token(String invalidToken) {
        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.extractUserInfoFromJWTToken(invalidToken)).isInstanceOf(NotAcceptableException.class)
                .hasMessageContaining("Token not set");
    }

    @ParameterizedTest
    @ValueSource(strings = { "invalid_token", "abcde",
            "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJzdWIiOiJ1c2VyMiIsImVtYWlsIjoidXNlcjJAZW1haWwiLCJpYXQiOjE3MzgyMjIyNTQsImV4cCI6MTczODMwODY1NH0.B7eSs3c3McMAvG8rHZVasIP_d-OWcDy5jPBpS4Ltest" })
    void extractUserInfoFromJWTToken_throws_not_acceptable_exception_for_invalid_token(String invalidToken) {
        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.extractUserInfoFromJWTToken(invalidToken)).isInstanceOf(NotAcceptableException.class)
                .hasMessageContaining("Invalid token");
    }
}