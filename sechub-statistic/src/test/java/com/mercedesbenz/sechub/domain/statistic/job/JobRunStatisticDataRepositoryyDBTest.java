// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.statistic.job;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
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

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@ActiveProfiles({ Profiles.TEST/* , Profiles.SQL_TRACE */ })
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { JobRunStatisticDataRepositoryyDBTest.SimpleTestConfiguration.class, JobStatisticRepository.class })
public class JobRunStatisticDataRepositoryyDBTest {
    private static final String TEST_ID1 = "java";
    private static final JobRunStatisticDataType TEST_TYPE1 = JobRunStatisticDataType.FILES_LANG;
    private static final BigInteger TEST_VALUE1 = BigInteger.valueOf(1000L);

    @Autowired
    private JobRunStatisticDataRepository repositoryToTest;

    private UUID sechubJobUUID;

    @Before
    public void before() {
        sechubJobUUID = UUID.randomUUID();
    }

    @Test
    public void create_and_fetch_all_jobrunstatistic_data_by_execution_uuid() {
        /* prepare */
        JobRunStatisticData entity = new JobRunStatisticData();
        entity.setExecutionUUID(sechubJobUUID);
        entity.setType(TEST_TYPE1);
        entity.setId(TEST_ID1);
        entity.setValue(TEST_VALUE1);

        /* execute */
        repositoryToTest.save(entity);

        /* test */
        List<JobRunStatisticData> found = repositoryToTest.findAllByExecutionUUID(sechubJobUUID);
        assertFalse(found.isEmpty());
        assertEquals(1, found.size());
        JobRunStatisticData data = found.iterator().next();
        assertEquals(TEST_TYPE1, data.type);
        assertEquals(TEST_ID1, data.id);
        assertEquals(TEST_VALUE1, data.value);
        assertNotNull(data.timeStamp); // must be set by database
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
