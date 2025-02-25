// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.user;

import static com.mercedesbenz.sechub.test.RestDocPathParameter.ONE_TIME_TOKEN;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.domain.administration.TestAdministrationSecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.spring.security.SecHubSecurityProperties;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { AnonymousUserRestController.class })
@ActiveProfiles({ Profiles.TEST })
@Import(TestAdministrationSecurityConfiguration.class)
public class AnonymousUserRestControllerTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserEmailAddressUpdateService userEmailAddressUpdateService;
    @MockBean
    private SecHubSecurityProperties secHubSecurityProperties;

    @Test
    @WithAnonymousUser
    public void anonymous_user_verifies_new_mail_address_with_one_time_token() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAnonymousUserVerifiesMailAddress(ONE_TIME_TOKEN.pathElement());

        /* @formatter:off */
        /* execute + test */
        this.mockMvc.perform(
                        get(apiEndpoint,"token1"))
                .andExpect(status().isNoContent());
        /* @formatter:on */
    }

}