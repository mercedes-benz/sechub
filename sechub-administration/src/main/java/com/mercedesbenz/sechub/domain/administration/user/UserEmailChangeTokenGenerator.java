package com.mercedesbenz.sechub.domain.administration.user;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.error.NotAcceptableException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

@Component
public class UserEmailChangeTokenGenerator {

    @Value("${jwt.secret}")
    private String secretKey;
    // todo user key from AES (issue #3648)

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours

    /**
     * Generates a JWT token for the given email with a 24-hour expiration time
     *
     * @param email the new user email address to include in the token
     * @return the generated JWT token
     */
    public String generateToken(String email) {
        return Jwts.builder().setSubject(email).setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }

    /**
     * Extracts the new user email from the given JWT token and validates its
     * expiration
     *
     * @param token the JWT token
     * @return the email extracted from the token
     * @throws NotAcceptableException if the token is invalid or expired
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

            String email = claims.getSubject();
            Date expiration = claims.getExpiration();

            if (expiration.before(new Date())) {
                throw new NotAcceptableException("Token expired");
            }
            return email;

        } catch (SignatureException e) {
            throw new NotAcceptableException("Invalid token signature");
        } catch (Exception e) {
            throw new NotAcceptableException("Invalid token");
        }
    }
}
