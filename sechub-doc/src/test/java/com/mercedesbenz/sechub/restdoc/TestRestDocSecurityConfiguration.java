// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.commons.core.shutdown.ApplicationShutdownHandler;
import com.mercedesbenz.sechub.sharedkernel.security.SecHubSecurityConfiguration;

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

}
