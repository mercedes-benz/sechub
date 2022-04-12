// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

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
public class SchedulerSmokeSpringBootTest {

    @Autowired
    private SchedulerRestController controller;

    @Autowired
    SchedulerSourcecodeUploadService uploadService;

    @Test
    public void context_loads_and_some_defaults_are_as_expected() throws Exception {
        // see https://spring.io/guides/gs/testing-web/ for details about testing with
        // spring MVC test
        assertThat(controller).isNotNull(); // we test that we got the controller. Means - the spring container context
                                            // has been loaded successfully!

        /* check defaults injected by container are as expected */
        assertTrue(uploadService.validateZip);
        assertTrue(uploadService.validateChecksum);
    }

}
