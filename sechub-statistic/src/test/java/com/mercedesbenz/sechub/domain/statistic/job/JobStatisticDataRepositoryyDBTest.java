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
@ContextConfiguration(classes = { JobStatisticDataRepositoryyDBTest.SimpleTestConfiguration.class, JobStatisticRepository.class })
public class JobStatisticDataRepositoryyDBTest {
    private static final String TEST_ID1 = "id1";
    private static final JobStatisticDataType TEST_TYPE1 = JobStatisticDataType.UPLOAD_SOURCES;
    private static final BigInteger TEST_VALUE1 = BigInteger.valueOf(1000L);

    @Autowired
    private JobStatisticDataRepository repositoryToTest;

    private UUID sechubJobUUID;

    @Before
    public void before() {
        sechubJobUUID = UUID.randomUUID();
    }

    @Test
    public void create_and_fetch_all_jobstatistic_data_by_sechub_jobuuid() {
        /* prepare */
        JobStatisticData entity1 = new JobStatisticData();
        entity1.setSechubJobUUID(sechubJobUUID);
        entity1.setType(TEST_TYPE1);
        entity1.setId(TEST_ID1);
        entity1.setValue(TEST_VALUE1);

        JobStatisticData entity2 = new JobStatisticData();
        entity2.setSechubJobUUID(UUID.randomUUID()); // other job...
        entity2.setType(TEST_TYPE1);
        entity2.setId(TEST_ID1);
        entity2.setValue(TEST_VALUE1.add(BigInteger.valueOf(2L)));

        /* execute */
        repositoryToTest.save(entity1);
        repositoryToTest.save(entity2);

        /* test */
        List<JobStatisticData> found = repositoryToTest.findAllBySechubJobUUID(sechubJobUUID);
        assertFalse(found.isEmpty());
        assertEquals(1, found.size());

        JobStatisticData data = found.iterator().next();
        assertEquals(sechubJobUUID, data.sechubJobUUID);
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
