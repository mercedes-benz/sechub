// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
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

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@ActiveProfiles({ Profiles.TEST/* , Profiles.SQL_TRACE */ })
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { JobStatisticRepositoryyDBTest.SimpleTestConfiguration.class, JobStatisticRepository.class })
public class JobStatisticRepositoryyDBTest {

    @Autowired
    private JobStatisticRepository repositoryToTest;

    private UUID sechubJobUUID;

    @Before
    public void before() {
        sechubJobUUID = UUID.randomUUID();
    }

    @Test
    public void create_and_fetch_jobstatistic_by_sechub_jobuuid() {
        /* prepare */
        JobStatistic entity = new JobStatistic();
        entity.projectId = "project1";
        entity.sechubJobUUID = sechubJobUUID;
        entity.created = LocalDateTime.now(); // job creation time, must be set

        /* execute */
        JobStatistic savedEntity = repositoryToTest.save(entity);

        /* test */
        assertNotNull(savedEntity.getCreated());

        Optional<JobStatistic> found = repositoryToTest.findById(sechubJobUUID);
        assertTrue(found.isPresent());
        assertEquals("project1", found.get().getProjectId());
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
