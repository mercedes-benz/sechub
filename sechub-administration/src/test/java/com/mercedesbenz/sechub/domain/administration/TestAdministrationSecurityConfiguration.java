// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;
import com.mercedesbenz.sechub.sharedkernel.security.SecHubSecurityConfiguration;
import com.mercedesbenz.sechub.spring.security.OAuth2OpaqueTokenIntrospectionResponseCryptoAccessProvider;

@Configuration
@Import(SecHubSecurityConfiguration.class)
public class TestAdministrationSecurityConfiguration {

    @Bean
    RestTemplate restTemplate() {
        return mock();
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
