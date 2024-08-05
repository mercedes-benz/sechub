// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecHubEncryptionStatusTest {

    @Test
    void json_transformation_works_as_expected() {
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
        String json = status.toJSON();

        /* test */
        assertNotNull(json);

        /* execute 2 */
        SecHubEncryptionStatus status2 = SecHubEncryptionStatus.fromString(json);

        /* test 2 */

        List<SecHubDomainEncryptionStatus> domains2 = status2.getDomains();
        assertThat(domains2).hasSize(1);

        SecHubDomainEncryptionStatus schedulerStatus2 = domains2.iterator().next();
        assertThat(schedulerStatus2.getName()).isEqualTo("schedule");

        List<SecHubDomainEncryptionData> data2 = schedulerStatus2.getData();
        assertThat(data2).hasSize(2);

        Iterator<SecHubDomainEncryptionData> iterator = data2.iterator();
        SecHubDomainEncryptionData schedule2Entry0 = iterator.next();
        SecHubDomainEncryptionData schedule2Entry1 = iterator.next();
        /* @formatter:off */
        assertThat(schedule2Entry0.getAlgorithm()).isEqualTo(SecHubCipherAlgorithm.NONE);
        assertThat(schedule2Entry0.getId()).isEqualTo("0");
        assertThat(schedule2Entry0.getPasswordSource().getType()).isEqualTo(SecHubCipherPasswordSourceType.NONE);
        assertThat(schedule2Entry0.getPasswordSource().getData()).isNull();
        assertThat(schedule2Entry0.getUsage()).containsAllEntriesOf(
                Map.of(
                        "job.state.canceled", 0L,
                        "job.state.ended", 0L,
                        "job.state.initialized", 2L
                        ));

        assertThat(schedule2Entry1.getAlgorithm()).isEqualTo(SecHubCipherAlgorithm.AES_GCM_SIV_128);
        assertThat(schedule2Entry1.getId()).isEqualTo("1");
        assertThat(schedule2Entry1.getPasswordSource().getType()).isEqualTo(SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE);
        assertThat(schedule2Entry1.getPasswordSource().getData()).isEqualTo("SECRET_1");
        assertThat(schedule2Entry1.getUsage()).containsAllEntriesOf(
                Map.of(
                        "job.state.canceled", 100L,
                        "job.state.ended", 200L,
                        "job.state.initialized", 1L
                        ));
        /* @formatter:on */

    }

    @BeforeEach
    public void beforeEach() throws Exception {

    }

}
