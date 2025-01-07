// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

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

import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig.ScanProjectConfigCompositeKey;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = { ScanProjectConfig.class, ScanProjectConfigRepositoryDBTest.SimpleTestConfiguration.class })
public class ScanProjectConfigRepositoryDBTest {

    @Autowired
    private ScanProjectConfigRepository repository;

    @Autowired
    TestEntityManager testEntityManager;

    @BeforeEach
    void beforeEach() {

    }

    @Test
    void storing_value_with_utf_8_3_xx4097_a_character_is_possible() throws Exception {
        /* prepare */
        StringBuilder sb = new StringBuilder();
        char oneByteChar = 'a'; // one byte in UTF-8... we store it 3 times... so old max reached
        for (int i = 0; i < (3 * 4097); i++) {
            sb.append(oneByteChar);
        }
        String data = sb.toString();

        ScanProjectConfig config = createNewScanProjectConfig("project1", "mykey1");
        config.setData(data);

        /* execute */
        repository.save(config);

        /* test */
        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findById(config.getKey()).get().getData()).isEqualTo(data);
    }

    @Test
    void given_3_stored_objects_2_for_project1_1_for_project2_a_delete_all_for_project1_does_only_delete_project1_parts() throws Exception {
        /* prepare */
        ScanProjectConfig config1_1 = createNewScanProjectConfig("project1", "config1.1");
        ScanProjectConfig config1_2 = createNewScanProjectConfig("project1", "config1.2");
        ScanProjectConfig config2_1 = createNewScanProjectConfig("project2", "config2.1");

        repository.save(config1_1);
        repository.save(config1_2);
        repository.save(config2_1);

        /* check preconditions */
        assertThat(repository.count()).isEqualTo(3);
        assertThat(repository.findById(config1_1.getKey())).isNotEmpty();
        assertThat(repository.findById(config1_2.getKey())).isNotEmpty();
        assertThat(repository.findById(config2_1.getKey())).isNotEmpty();
        /* execute */
        repository.deleteAllConfigurationsForProject("project1");

        /* test */
        testEntityManager.clear(); // necessary, otherwise findbyId returns old values (from cache?)

        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findById(config2_1.getKey())).isNotEmpty();
    }

    @Test
    void given_3_stored_objects_2_for_project1_1_for_project2_a_delete_all_for_given_config_ids_and_value_deletes_no_others() throws Exception {
        /* prepare */
        String keyA = "configA";
        String keyB = "configB";
        String keyC = "configC";

        String value1 = "val1";
        String value2 = "val2";

        ScanProjectConfig config1A1 = createNewScanProjectConfig("project1", keyA, value1); // will be deleted
        ScanProjectConfig config1B1 = createNewScanProjectConfig("project1", keyB, value1);
        ScanProjectConfig config1C1 = createNewScanProjectConfig("project1", keyC, value1); // will be deleted

        ScanProjectConfig config2A1 = createNewScanProjectConfig("project2", keyA, value1); // will be deleted
        ScanProjectConfig config2B1 = createNewScanProjectConfig("project2", keyB, value1);
        ScanProjectConfig config2C1 = createNewScanProjectConfig("project2", keyC, value1); // will be deleted

        ScanProjectConfig config3A2 = createNewScanProjectConfig("project3", keyA, value2);
        ScanProjectConfig config3B2 = createNewScanProjectConfig("project3", keyB, value2);
        ScanProjectConfig config3C2 = createNewScanProjectConfig("project3", keyC, value2);

        repository.save(config1A1);
        repository.save(config1B1);
        repository.save(config1C1);

        repository.save(config2A1);
        repository.save(config2B1);
        repository.save(config2C1);

        repository.save(config3A2);
        repository.save(config3B2);
        repository.save(config3C2);

        /* check preconditions */
        assertThat(repository.count()).isEqualTo(9);

        /* execute */
        repository.deleteAllConfigurationsOfGivenConfigIdsAndValue(Set.of(keyA, keyC), value1);

        /* test */
        testEntityManager.clear(); // necessary, otherwise findbyId returns old values (from cache?)

        assertThat(repository.count()).isEqualTo(5);

        assertThat(repository.findById(config1B1.getKey())).isNotEmpty();
        assertThat(repository.findById(config2B1.getKey())).isNotEmpty();
        assertThat(repository.findById(config3A2.getKey())).isNotEmpty();
        assertThat(repository.findById(config3B2.getKey())).isNotEmpty();
        assertThat(repository.findById(config3C2.getKey())).isNotEmpty();

        assertThat(repository.findById(config1A1.getKey())).isEmpty();
        assertThat(repository.findById(config1C1.getKey())).isEmpty();
        assertThat(repository.findById(config2A1.getKey())).isEmpty();
        assertThat(repository.findById(config2C1.getKey())).isEmpty();
    }

    private ScanProjectConfig createNewScanProjectConfig(String projectId, String pseudoConfigId) {
        ScanProjectConfigCompositeKey key = new ScanProjectConfigCompositeKey(pseudoConfigId, projectId);
        return new ScanProjectConfig(key);
    }

    private ScanProjectConfig createNewScanProjectConfig(String projectId, String pseudoConfigId, String data) {
        ScanProjectConfig config = createNewScanProjectConfig(projectId, pseudoConfigId);
        config.setData(data);
        return config;
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
