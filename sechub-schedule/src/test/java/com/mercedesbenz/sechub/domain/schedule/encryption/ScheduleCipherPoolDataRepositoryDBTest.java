package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ScheduleCipherPoolDataRepository.class, ScheduleCipherPoolDataRepositoryDBTest.SimpleTestConfiguration.class })
class ScheduleCipherPoolDataRepositoryDBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ScheduleCipherPoolDataRepository repositoryToTest;

    @Test
    void fetchAllCipherPoolIds_nothing_found_returns_empty_set() {

        /* execute */
        Set<Long> result = repositoryToTest.fetchAllCipherPoolIds();

        /* test */
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void fetchAllCipherPoolIds_one_entry() throws Exception {

        /* prepare */
        Set<Long> createdIds = new LinkedHashSet<>();

        createdIds.add(createEntry());

        /* execute */
        Set<Long> result = repositoryToTest.fetchAllCipherPoolIds();

        /* test */
        assertThat(result).isNotNull().isNotEmpty().containsAll(createdIds).hasSize(1);

    }

    @Test
    void fetchAllCipherPoolIds_two_entries() throws Exception {
        /* prepare */
        Set<Long> createdIds = new LinkedHashSet<>();

        createdIds.add(createEntry());
        createdIds.add(createEntry());

        /* execute */
        Set<Long> result = repositoryToTest.fetchAllCipherPoolIds();

        /* test */
        assertThat(result).isNotNull().isNotEmpty().containsAll(createdIds);

    }

    private long createEntry() {
        ScheduleCipherPoolData data1 = new ScheduleCipherPoolData();
        data1.secHubCipherPasswordSourceType = SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE;
        data1.created = LocalDateTime.now();
        data1.algorithm = SecHubCipherAlgorithm.NONE;
        data1.testEncrypted = "test".getBytes(Charset.forName("UTF-8"));
        data1.testInitialVector = new byte[] {};
        data1.testText = "test";

        ScheduleCipherPoolData result = entityManager.persist(data1);
        return result.getId();
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
