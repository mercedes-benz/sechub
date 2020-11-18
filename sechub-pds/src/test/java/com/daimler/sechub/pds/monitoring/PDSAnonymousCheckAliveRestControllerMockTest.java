// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.monitoring;

import static com.daimler.sechub.test.TestURLBuilder.*;
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

import com.daimler.sechub.pds.PDSProfiles;
import com.daimler.sechub.pds.security.AbstractAllowPDSAPISecurityConfiguration;
import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(PDSAnonymousCheckAliveRestController.class)
/* @formatter:off */
@ContextConfiguration(classes = { 
        PDSAnonymousCheckAliveRestController.class,
		PDSAnonymousCheckAliveRestControllerMockTest.SimpleTestConfiguration.class })
/* @formatter:on */
@WithMockUser(authorities = PDSRoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles(PDSProfiles.TEST)
public class PDSAnonymousCheckAliveRestControllerMockTest {

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();
	
	@Autowired
	private MockMvc mockMvc;

	@Test
    public void a_get_execution_status_calls_executionService_and_returns_result() throws Exception {
        /* prepare */
        
        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                head(https(PORT_USED).pds().buildAnonymousCheckAlive())
                ).
                    andExpect(status().isOk()
                );

        /* @formatter:on */

    }


	@TestConfiguration
	@Profile(PDSProfiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowPDSAPISecurityConfiguration {

	}

}
