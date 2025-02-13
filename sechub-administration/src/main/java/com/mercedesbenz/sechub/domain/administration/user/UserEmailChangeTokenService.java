// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.spring.security.AES256Encryption;

@Service
public class UserEmailChangeTokenService {
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final AES256Encryption aes256Encryption;
    private final Clock clock;

    public UserEmailChangeTokenService(AES256Encryption aes256Encryption) {
        this.aes256Encryption = aes256Encryption;
        this.clock = Clock.systemUTC();
    }

    public String generateToken(UserEmailChangeRecord userEmailChangeRecord, String baseUrl) {
        validateString(baseUrl, "Base URL must not be null or blank!");

        UserEmailChangeToken userEmailChangeToken = new UserEmailChangeToken(userEmailChangeRecord.userId(), userEmailChangeRecord.newEmail(),
                clock.instant().toString());
        userEmailChangeToken.validate();
        String json = userEmailChangeToken.toJSON();

        byte[] bytes = aes256Encryption.encrypt(json);
        return ENCODER.encodeToString(bytes);
    }

    public UserEmailChangeRecord extractUserInfoFromToken(String token) {
        validateString(token, "Token must not be null or blank!");

        byte[] bytes = DECODER.decode(token);
        String decryptedToken = aes256Encryption.decrypt(bytes);

        UserEmailChangeToken userEmailChangeToken = UserEmailChangeToken.fromJSON(decryptedToken);

        if (userEmailChangeToken == null) {
            throw new BadRequestException("Token is invalid!");
        }
        userEmailChangeToken.validate();

        Instant instant = Instant.parse(userEmailChangeToken.getTimestamp());
        if (instant.plusSeconds(EXPIRATION_TIME).isBefore(clock.instant())) {
            throw new BadRequestException("Token has expired!");
        }

        return new UserEmailChangeRecord(userEmailChangeToken.getUserId(), userEmailChangeToken.getEmailAddress());
    }

    private void validateString(String string, String message) {
        Objects.requireNonNull(string);
        if (string.isBlank()) {
            throw new BadRequestException(message);
        }
    }
}
