// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webserver.security;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;
import com.mercedesbenz.sechub.webserver.encryption.AES256Encryption;
import com.mercedesbenz.sechub.webserver.server.ManagementServerProperties;
import com.mercedesbenz.sechub.webserver.server.ServerProperties;
import com.mercedesbenz.sechub.webserver.server.ServerPropertiesConfiguration;

@WebMvcTest
@ActiveProfiles("classic-auth-enabled")
@TestPropertySource(locations = "classpath:application-test.yml", factory = YamlPropertyLoaderFactory.class)
class SecurityConfigurationTest {

    private final MockMvc mockMvc;
    private final ServerProperties serverProperties;
    private final ManagementServerProperties managementServerProperties;

    @Autowired
    SecurityConfigurationTest(MockMvc mockMvc, ServerProperties serverProperties, ManagementServerProperties managementServerProperties) {
        this.mockMvc = mockMvc;
        this.serverProperties = serverProperties;
        this.managementServerProperties = managementServerProperties;
    }

    @Test
    void actuator_is_accessible_anonymously_at_management_port() throws Exception {
        /* prepare */
        String url = "http://localhost:%d/actuator".formatted(managementServerProperties.getPort());

        /* execute & test */
        getAndExpect(url, HttpStatus.OK);
    }

    @Test
    void actuator_is_not_accessible_at_server_port() throws Exception {
        /* prepare */
        String url = "http://localhost:%d/actuator".formatted(serverProperties.getPort());

        /* execute & test */
        getAndExpect(url, HttpStatus.FORBIDDEN);
    }

    @Test
    void public_path_is_accessible_at_management_port() throws Exception {
        /* prepare */
        String url = "http://localhost:%d/login".formatted(managementServerProperties.getPort());

        /* execute & test */
        getAndExpect(url, HttpStatus.FORBIDDEN);
    }

    @Test
    void public_path_is_accessible_at_server_port() throws Exception {
        /* prepare */
        String url = "http://localhost:%d/login".formatted(serverProperties.getPort());

        /* execute & test */
        getAndExpect(url, HttpStatus.OK);
    }

    private void getAndExpect(String path, HttpStatus httpStatus) throws Exception {
        /* @formatter:off */
        mockMvc
                .perform(MockMvcRequestBuilders.get(path))
                .andExpect(status().is(httpStatus.value()));
        /* @formatter:on */
    }

    @Configuration
    @Import({ SecurityConfiguration.class, ServerPropertiesConfiguration.class })
    static class TestConfig {

        @Bean
        TestSecurityController testSecurityController() {
            return new TestSecurityController();
        }

        @Bean
        AES256Encryption aes256Encryption() {
            return mock();
        }
    }
}
