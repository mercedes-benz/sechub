// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.daimler.sechub.pds.job.PDSJobRestController;

/**
 * Test simply starts up spring container and checks a schedule rest controller
 * can be initiated. So just a smoke test
 *
 * @author Albert Tregnaghi
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
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
