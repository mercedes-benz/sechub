// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.monitoring;

import static com.mercedesbenz.sechub.test.PDSTestURLBuilder.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
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

import com.mercedesbenz.sechub.pds.PDSProfiles;
import com.mercedesbenz.sechub.pds.commons.core.PDSJSONConverter;
import com.mercedesbenz.sechub.pds.security.AbstractAllowPDSAPISecurityConfiguration;
import com.mercedesbenz.sechub.pds.security.PDSRoleConstants;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(PDSAdminMonitoringRestController.class)
/* @formatter:off */
@ContextConfiguration(classes = {
        PDSAdminMonitoringRestController.class,
        PDSAdminMonitoringRestControllerMockTest.SimpleTestConfiguration.class })
/* @formatter:on */
@WithMockUser(authorities = PDSRoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles(PDSProfiles.TEST)
public class PDSAdminMonitoringRestControllerMockTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PDSMonitoringStatusService mockedMonitoringStatusService;

    private PDSMonitoring result;

    @Before
    public void before() throws Exception {
        /* prepare */
        result = PDSMonitoringTestDataUtil.createTestMonitoringWith2ClusterMembers();
    }

    @Test
    public void a_get_execution_status_calls_executionService_and_returns_result() throws Exception {

        String expectedJSON = PDSJSONConverter.get().toJSON(result);

        when(mockedMonitoringStatusService.getMonitoringStatus()).thenReturn(result);

        /* execute + test */
        /* @formatter:off */
        this.mockMvc.perform(
                get(https(PORT_USED).buildAdminGetMonitoringStatus())
                ).
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
