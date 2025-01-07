// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.sharedkernel.security.SecHubSecurityConfiguration;

@Configuration
@Import(SecHubSecurityConfiguration.class)
public class TestAdministrationSecurityConfiguration {

    @Bean
    RestTemplate restTemplate() {
        return mock();
    }
}
