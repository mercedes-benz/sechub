// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static org.mockito.Mockito.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;
import com.mercedesbenz.sechub.sharedkernel.security.SecHubSecurityConfiguration;
import com.mercedesbenz.sechub.spring.security.OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider;

@Import(SecHubSecurityConfiguration.class)
class TestRestDocSecurityConfiguration {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ApplicationShutdownHandler applicationShutdownHandler() {
        return mock();
    }

    @Bean
    OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider cryptoAccessProvider() {
        return mock();
    }
}
