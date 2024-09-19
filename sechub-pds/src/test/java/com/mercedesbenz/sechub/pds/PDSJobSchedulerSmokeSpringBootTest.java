// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;
import com.mercedesbenz.sechub.pds.job.PDSJobRestController;

/**
 * Test simply starts up spring container and checks a schedule rest controller
 * can be initiated. So just a smoke test
 *
 * @author Albert Tregnaghi
 *
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-pds_test.yml")
@ActiveProfiles(PDSProfiles.TEST)
public class PDSJobSchedulerSmokeSpringBootTest {

    @Autowired
    private PDSJobRestController controller;

    @Test
    public void contextLoads() throws Exception {
        // see https://spring.io/guides/gs/testing-web/ for details about testing with
        // spring MVC test
        assertThat(controller).isNotNull(); // we just test that we got he controller. Means - the spring container context
                                            // has been loaded successfully!
    }

}
