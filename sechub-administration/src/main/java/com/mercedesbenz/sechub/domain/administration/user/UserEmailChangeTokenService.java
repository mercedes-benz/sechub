package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Date;
import java.util.Optional;

import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.core.security.CryptoAccess;
import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;
import com.mercedesbenz.sechub.spring.security.SecHubSecurityProperties;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class UserEmailChangeTokenService {
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours
    private static final String CLAIM_EMAIL = "email";
    private final SecHubSecurityProperties secHubSecurityProperties;
    private final SealedObject sealedSecretKey;

    public UserEmailChangeTokenService(SecHubSecurityProperties secHubSecurityProperties) {
        this.secHubSecurityProperties = secHubSecurityProperties;
        this.sealedSecretKey = CryptoAccess.CRYPTO_STRING.seal(secHubSecurityProperties.getEncryptionProperties().getSecretKey());
    }

    /**
     * Generates a JWT token for the given user info with a 24-hour expiration time
     *
     * @param userInfo the user info containing the user ID and the new user email
     * @return the generated JWT token
     */
    public String generateToken(UserInfo userInfo, String baseUrl) {
        validateInputs(userInfo, baseUrl);

        /* @formatter:off */
        return Jwts.builder()
                .issuer(baseUrl)
                .subject(userInfo.getUserId())
                .claim(CLAIM_EMAIL, userInfo.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSealedSecretKey())
                .compact();
        /* @formatter:on */
    }

    /**
     * Extracts the user info from the given JWT token and validates its expiration
     *
     * @param token the JWT token
     * @return the user info containing the user ID and the new user email extracted
     *         from the token
     * @throws NotAcceptableException if the token is invalid or expired
     */
    public UserInfo extractUserInfoFromJWTToken(String token) {
        if (token == null || token.isBlank()) {
            throw new NotAcceptableException("Token not set");
        }

        try {
            /* @formatter:off */
            Claims claims = Jwts.parser()
                    .verifyWith(getSealedSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            /* @formatter:on */

            String userId = claims.getSubject();
            String email = claims.get(CLAIM_EMAIL).toString();
            Date expiration = claims.getExpiration();

            if (expiration.before(new Date())) {
                throw new NotAcceptableException("Token expired");
            }

            return new UserInfo(userId, email);
        } catch (JwtException e) {
            throw new NotAcceptableException("Invalid token");
        }
    }

    private SecretKey getSealedSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(CryptoAccess.CRYPTO_STRING.unseal(sealedSecretKey));
        // HMAC + SHA256 common signing algorithm for JWT
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private void validateInputs(UserInfo userInfo, String baseUrl) {
        Optional.ofNullable(userInfo).orElseThrow(() -> new NotAcceptableException("User info not set"));
        Optional.ofNullable(baseUrl).filter(url -> !url.isBlank()).orElseThrow(() -> new NotAcceptableException("Base URL not set"));
    }
}
