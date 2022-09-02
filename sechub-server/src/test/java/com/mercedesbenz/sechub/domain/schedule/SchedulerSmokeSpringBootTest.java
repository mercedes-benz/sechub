// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.mercedesbenz.sechub.sharedkernel.RoleConstants;

/*
 * Smoke tests which checks that sechub-server spring boot container can be started and
 * some defaults are as expected
 *
 * @author Albert Tregnaghi
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@WithMockUser(authorities = { RoleConstants.ROLE_USER })
public class SchedulerSmokeSpringBootTest {

    @Autowired
    private SchedulerRestController schedulerRestController;

    @Autowired
    SchedulerSourcecodeUploadConfiguration sourcecodeUploadConfiguration;

    @Autowired
    SchedulerBinariesUploadConfiguration binariesUploadConfiguration;

    @Test
    public void context_loads_and_some_defaults_are_as_expected() throws Exception {
        // see https://spring.io/guides/gs/testing-web/ for details about testing with
        // spring MVC test
        assertThat(schedulerRestController).isNotNull(); // we test that we got the schedulerRestController. Means - the spring container
                                                         // context
        // has been loaded successfully!

        /* check configuration defaults injected by container are as expected */
        assertTrue(sourcecodeUploadConfiguration.isZipValidationEnabled());
        assertTrue(sourcecodeUploadConfiguration.isChecksumValidationEnabled());

        assertEquals(50 * 1024 * 1024, binariesUploadConfiguration.getMaxUploadSizeInBytes());
    }

}
