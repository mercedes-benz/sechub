// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.sharedkernel.error.BadRequestException;
import com.mercedesbenz.sechub.spring.security.AES256Encryption;

@Service
public class UserEmailChangeTokenService {
    private static final long EXPIRATION_TIME_MILLIS = Duration.ofHours(24).toMillis();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final AES256Encryption aes256Encryption;

    public UserEmailChangeTokenService(AES256Encryption aes256Encryption) {
        this.aes256Encryption = requireNonNull(aes256Encryption);
    }

    public String generateToken(UserEmailChangeRequest userEmailChangeRequest) {
        requireNonNull(userEmailChangeRequest);
        UserEmailChangeToken userEmailChangeToken = new UserEmailChangeToken(userEmailChangeRequest.userId(), userEmailChangeRequest.newEmail(),
                Instant.now().toString());
        String json = userEmailChangeToken.toJSON();

        byte[] bytes = aes256Encryption.encrypt(json);
        return ENCODER.encodeToString(bytes);
    }

    public UserEmailChangeRequest extractUserInfoFromToken(String token) {
        requireNonNull(token);

        byte[] bytes = DECODER.decode(token);
        String decryptedToken = aes256Encryption.decrypt(bytes);

        UserEmailChangeToken userEmailChangeToken = UserEmailChangeToken.createFromJSON(decryptedToken);

        if (userEmailChangeToken == null) {
            throw new BadRequestException("Token is invalid!");
        }

        Instant instant = Instant.parse(userEmailChangeToken.getTimestamp());
        if (instant.plusMillis(EXPIRATION_TIME_MILLIS).isBefore(Instant.now())) {
            throw new BadRequestException("Token has expired!");
        }

        return new UserEmailChangeRequest(userEmailChangeToken.getUserId(), userEmailChangeToken.getEmailAddress());
    }
}
