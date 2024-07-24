// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SecHubEncryptionStatusTest {

    @Test
    void test() {
        /* prepare */
        SecHubEncryptionStatus status = new SecHubEncryptionStatus();

        SecHubDomainEncryptionData scheduleEntry0 = new SecHubDomainEncryptionData();
        scheduleEntry0.setAlgorithm(SecHubCipherAlgorithm.NONE);
        scheduleEntry0.setId("0");
        scheduleEntry0.getPasswordSource().setData(null);
        scheduleEntry0.getPasswordSource().setType(SecHubCipherPasswordSourceType.NONE);

        scheduleEntry0.getUsage().put("job.state.canceled", 0L);
        scheduleEntry0.getUsage().put("job.state.ended", 0L);
        scheduleEntry0.getUsage().put("job.state.initialized", 2L);

        SecHubDomainEncryptionData scheduleEntry1 = new SecHubDomainEncryptionData();
        scheduleEntry1.setAlgorithm(SecHubCipherAlgorithm.AES_GCM_SIV_128);
        scheduleEntry1.setId("1");
        scheduleEntry1.getPasswordSource().setData("SECRET_1");
        scheduleEntry1.getPasswordSource().setType(SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE);

        scheduleEntry1.getUsage().put("job.state.canceled", 100L);
        scheduleEntry1.getUsage().put("job.state.ended", 200L);
        scheduleEntry1.getUsage().put("job.state.initialized", 1L);

        SecHubDomainEncryptionStatus schedulerStatus = new SecHubDomainEncryptionStatus();
        schedulerStatus.setName("schedule");
        schedulerStatus.getData().add(scheduleEntry0);
        schedulerStatus.getData().add(scheduleEntry1);

        status.getDomains().add(schedulerStatus);

        /* execute */
        String json = status.toFormattedJSON();

        /* test */
        System.out.println(json);
        assertNotNull(json);
    }

}
