// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig.ScanProjectConfigCompositeKey;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ScanProjectConfig.class, ScanProjectConfigRepositoryDBTest.SimpleTestConfiguration.class })
public class ScanProjectConfigRepositoryDBTest {

    @Autowired
    private ScanProjectConfigRepository repository;

    @Before
    public void before() {

    }

    @Test
    public void storing_value_with_utf_8_3_xx4097_a_character_is_possible() throws Exception {
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
        assertEquals(1, repository.count());
        assertEquals(data, repository.findById(config.getKey()).get().getData());
    }

    @Test
    public void given_3_stored_objects_2_for_project1_1_for_project2_a_delete_all_for_project1_does_only_delete_project1_parts() throws Exception {
        /* prepare */
        ScanProjectConfig config1_1 = createNewScanProjectConfig("project1", "config1.1");
        ScanProjectConfig config1_2 = createNewScanProjectConfig("project1", "config1.2");
        ScanProjectConfig config2_1 = createNewScanProjectConfig("project2", "config2.1");

        repository.save(config1_1);
        repository.save(config1_2);
        repository.save(config2_1);

        /* check preconditions */
        assertEquals(3, repository.count());
        assertNotNull(repository.findById(config1_1.getKey()));
        assertNotNull(repository.findById(config1_2.getKey()));
        assertNotNull(repository.findById(config2_1.getKey()));

        /* execute */
        repository.deleteAllConfigurationsForProject("project1");

        /* test */
        assertEquals(1, repository.count());
        assertNotNull(repository.findById(config2_1.getKey()));
    }

    private ScanProjectConfig createNewScanProjectConfig(String projectId, String pseudoConfigId) {
        ScanProjectConfigCompositeKey key = new ScanProjectConfigCompositeKey(pseudoConfigId, projectId);
        return new ScanProjectConfig(key);
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

}
