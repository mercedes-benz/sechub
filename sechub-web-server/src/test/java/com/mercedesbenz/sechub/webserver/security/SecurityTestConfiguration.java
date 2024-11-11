// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.mercedesbenz.sechub.testframework.spring.JwtMockAuthenticationTestConfiguration;
import com.mercedesbenz.sechub.webserver.encryption.AES256Encryption;

import jakarta.servlet.http.Cookie;

@TestConfiguration
@Import({ SecurityConfiguration.class, JwtMockAuthenticationTestConfiguration.class, OAuth2PropertiesConfig.class, AES256Encryption.class })
public class SecurityTestConfiguration {

    @Bean
    public RequestPostProcessor requestPostProcessor() {
        Cookie cookie = new Cookie(JwtMockAuthenticationTestConfiguration.ACCESS_TOKEN, JwtMockAuthenticationTestConfiguration.ENCRYPTED_JWT_B64_ENCODED);
        return new TestCookieRequestPostProcessor(cookie);
    }
}
