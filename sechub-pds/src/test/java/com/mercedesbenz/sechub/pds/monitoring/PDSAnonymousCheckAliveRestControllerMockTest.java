// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.monitoring;

import static com.mercedesbenz.sechub.test.PDSTestURLBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.pds.PDSProfiles;
import com.mercedesbenz.sechub.pds.security.PDSAPISecurityConfiguration;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(PDSAnonymousCheckAliveRestController.class)
/* @formatter:off */
@ContextConfiguration(classes = {
        PDSAnonymousCheckAliveRestController.class,
		PDSAnonymousCheckAliveRestControllerMockTest.SimpleTestConfiguration.class })
/* @formatter:on */
@WithMockUser(roles = PDSRoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles(PDSProfiles.TEST)
public class PDSAnonymousCheckAliveRestControllerMockTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void calling_check_alive_head_returns_HTTP_200() throws Exception {
        /* prepare */

        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                head(https(PORT_USED).buildAnonymousCheckAlive())
                ).
                    andExpect(status().isOk()
                );

        /* @formatter:on */

    }

    @Test
    public void calling_check_alive_get_returns_HTTP_200() throws Exception {
        /* prepare */

        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildAnonymousCheckAlive())
                ).
                    andExpect(status().isOk()
                );

        /* @formatter:on */

    }

    @TestConfiguration
    @Profile(PDSProfiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends PDSAPISecurityConfiguration {

    }

}
