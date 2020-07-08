// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.execution;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.daimler.sechub.pds.PDSJSONConverter;
import com.daimler.sechub.pds.PDSProfiles;
import com.daimler.sechub.pds.monitoring.PDSAdminMonitoringRestController;
import com.daimler.sechub.pds.security.AbstractAllowPDSAPISecurityConfiguration;
import com.daimler.sechub.pds.security.PDSRoleConstants;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(PDSAdminMonitoringRestController.class)
/* @formatter:off */
@ContextConfiguration(classes = { 
        PDSAdminMonitoringRestController.class,
        PDSExecutionService.class,
		PDSAdminExecutionRestControllerMockTest.SimpleTestConfiguration.class })
/* @formatter:on */
@WithMockUser(authorities = PDSRoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles(PDSProfiles.TEST)
public class PDSAdminExecutionRestControllerMockTest {

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();
	
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PDSExecutionService mockedExecutionService;

	@Test
    public void a_get_execution_status_calls_executionService_and_returns_result() throws Exception {
        /* prepare */
        PDSExecutionStatus result = new PDSExecutionStatus();
        result.jobsInQueue=5;
        result.queueMax=20;
        
        String expectedJSON = PDSJSONConverter.get().toJSON(result);
        
        when(mockedExecutionService.getExecutionStatus()).thenReturn(result);
        
        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).pds().buildAdminGetExecutionStatus())
                )./*andDo(print()).*/
                    andExpect(status().isOk()).
                    andExpect(content().json(expectedJSON)
                );

        /* @formatter:on */

    }


	@TestConfiguration
	@Profile(PDSProfiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowPDSAPISecurityConfiguration {

	}

}
