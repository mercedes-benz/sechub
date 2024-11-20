// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.mercedesbenz.sechub.testframework.spring.TestJwtMockAuthenticationConfiguration;
import com.mercedesbenz.sechub.webserver.encryption.AES256Encryption;

import jakarta.servlet.http.Cookie;

@TestConfiguration
@Import({ WebServerSecurityConfiguration.class, TestJwtMockAuthenticationConfiguration.class, OAuth2PropertiesConfig.class, AES256Encryption.class })
public class TestWebServerSecurityConfiguration {

    @Bean
    public RequestPostProcessor requestPostProcessor() {
        Cookie cookie = new Cookie(TestJwtMockAuthenticationConfiguration.ACCESS_TOKEN, TestJwtMockAuthenticationConfiguration.ENCRYPTED_JWT_B64_ENCODED);
        return new TestCookieRequestPostProcessor(cookie);
    }
}
