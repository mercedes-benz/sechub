// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import com.mercedesbenz.sechub.spring.security.AES256Encryption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.client.RestTemplate;

import com.mercedesbenz.sechub.sharedkernel.security.SecHubSecurityConfiguration;

import static org.mockito.Mockito.mock;

@Import(SecHubSecurityConfiguration.class)
class TestRestDocSecurityConfiguration {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
