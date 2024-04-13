// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = { SecHubJobDataRepository.class, SecHubJobDataRepositoryDBTest.SimpleTestConfiguration.class })
public class SecHubJobDataRepositoryDBTest {

    private static final String KEY1 = "id1";

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SecHubJobDataRepository jobRepository;

    @BeforeEach
    void before() {
    }

    @Test
    void deleteJobDataOlderThan_works() throws Exception {
        /* prepare */

        LocalDateTime before4Days = LocalDateTime.now().minusDays(4);
        LocalDateTime before5Days = LocalDateTime.now().minusDays(5);
        LocalDateTime before10Days = LocalDateTime.now().minusDays(10);

        UUID jobUUID = UUID.randomUUID();
        ScheduleSecHubJobData data1 = new ScheduleSecHubJobData(jobUUID, KEY1, "val1");
        data1.created = before5Days;
        data1.value = "val1";

        entityManager.persistAndFlush(data1);
        ScheduleSecHubJobData found = findDataByJobUUID(jobUUID);
        assertNotNull(found);

        /* execute 1 */
        int deleted = jobRepository.deleteJobDataOlderThan(before10Days);

        /* test 1 */
        assertEquals(0, deleted);
        jobRepository.flush();
        entityManager.clear();

        found = findDataByJobUUID(jobUUID);
        assertNotNull(found); // still exists because not older than 10 days

        /* execute 2 */
        deleted = jobRepository.deleteJobDataOlderThan(before4Days);

        /* test 2 */
        assertEquals(1, deleted);

        jobRepository.flush();
        entityManager.clear();

        found = findDataByJobUUID(jobUUID);
        assertNull(found); // may no longer exist

    }

    private ScheduleSecHubJobData findDataByJobUUID(UUID jobUUID) {
        return entityManager.find(ScheduleSecHubJobData.class, new ScheduleSecHubJobDataId(jobUUID, KEY1));
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
