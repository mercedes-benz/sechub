// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.spring.security;

import com.mercedesbenz.sechub.testframework.spring.YamlPropertyLoaderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;



@WithMockUser
@ExtendWith(SpringExtension.class)
@WebMvcTest(LoginController.class)
@ContextConfiguration(classes = { LoginController.class })
@EnableAutoConfiguration
@TestPropertySource(locations = "classpath:application-login-classic-test.yaml", factory = YamlPropertyLoaderFactory.class)
@EnableConfigurationProperties(SecHubSecurityProperties.class)
class LoginClassicControllerTest {

    private final MockMvc mockMvc;

    @Autowired
    LoginClassicControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void login_with_classic_mode_enabled() throws Exception {
        /* execute & test */
        mockMvc.perform(get("/login-page"))
                .andExpect(status().isOk())
                .andExpect(view().name("login.html"))
                .andExpect(model().attribute("isOAuth2Enabled", false))
                .andExpect(model().attribute("isClassicAuthEnabled", true))
                /* Tabs should not be visible */
                .andExpect(content().string(not(containsString("OAuth2"))))
                .andExpect(content().string(not(containsString("API Token (Classic)"))))
                /* OAuth2 Login Section should not be visible */
                .andExpect(content().string(not(containsString("You will be redirected to your OAuth2 Provider for authentication."))))
                /* Classic Login Section should be visible */
                .andExpect(content().string(containsString("Login with your provided User ID & API Token.")));



    }

}
