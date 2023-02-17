// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.sharedkernel.Profiles;

@ActiveProfiles({ Profiles.TEST/* , Profiles.SQL_TRACE */ })
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { JobRunStatisticRepositoryyDBTest.SimpleTestConfiguration.class, JobStatisticRepository.class })
public class JobRunStatisticRepositoryyDBTest {

    private static final String TEST_PROJECT1 = "project1";
    private static final LocalDateTime TEST_CREATED1 = LocalDateTime.now();
    private static final LocalDateTime TEST_ENDED1 = LocalDateTime.now();
    private static final TrafficLight TEST_TRAFFICLIGHT1 = TrafficLight.YELLOW;

    @Autowired
    private JobRunStatisticRepository repositoryToTest;

    private UUID sechubJobUUID;
    private UUID executionUUID;
    private static final boolean TEST_FAILED1 = true;

    @Before
    public void before() {
        sechubJobUUID = UUID.randomUUID();
        executionUUID = UUID.randomUUID();

    }

    @Test
    public void create_and_fetch_all_jobrunstatistic_by_sechub_jobuuid() {
        /* prepare */
        JobRunStatistic entity = new JobRunStatistic();
        entity.setProjectId(TEST_PROJECT1);
        entity.setCreated(TEST_CREATED1);
        entity.setEnded(TEST_ENDED1);
        entity.setSechubJobUUID(sechubJobUUID);
        entity.setExecutionUUID(executionUUID);
        entity.setTrafficLight(TEST_TRAFFICLIGHT1);
        entity.setFailed(TEST_FAILED1);

        /* execute */
        repositoryToTest.save(entity);

        /* test */
        List<JobRunStatistic> found = repositoryToTest.findAllBySechubJobUUID(sechubJobUUID);
        assertFalse(found.isEmpty());
        JobRunStatistic first = found.iterator().next();
        assertEquals(TEST_PROJECT1, first.projectId);
        assertEquals(TEST_CREATED1, first.created);
        assertEquals(sechubJobUUID, first.sechubJobUUID);
        assertEquals(executionUUID, first.executionUUID);
        assertEquals(TEST_TRAFFICLIGHT1, first.trafficLight);
        assertEquals(TEST_FAILED1, first.failed);
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
