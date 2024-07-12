// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionResult;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;

/*
 * Smoke tests which checks that sechub-server spring boot container can be started and
 * some defaults are as expected
 *
 * @author Albert Tregnaghi
 *
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@WithMockUser(roles = { RoleConstants.ROLE_USER })
@ActiveProfiles(Profiles.TEST)
public class SchedulerSmokeSpringBootTest {

    @Autowired
    private SchedulerRestController schedulerRestController;

    @Autowired
    SchedulerSourcecodeUploadConfiguration sourcecodeUploadConfiguration;

    @Autowired
    SchedulerBinariesUploadConfiguration binariesUploadConfiguration;

    @Autowired
    ScheduleEncryptionService encryptionService;

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

        // test encryption service is initialized and works
        String textToEncrypt = "i need encryption";
        ScheduleEncryptionResult encryptResult = encryptionService.encryptWithLatestCipher(textToEncrypt);
        String decrypted = encryptionService.decryptToString(encryptResult.getEncryptedData(), encryptResult.getCipherPoolId(),
                encryptResult.getInitialVector());
        assertEquals(textToEncrypt, decrypted);

    }

}
