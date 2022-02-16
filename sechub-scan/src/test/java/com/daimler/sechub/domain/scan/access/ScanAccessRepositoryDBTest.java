// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.access;

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

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ScanAccessRepository.class, ScanAccessRepositoryDBTest.SimpleTestConfiguration.class })
public class ScanAccessRepositoryDBTest {

    @Autowired
    private ScanAccessRepository repository;

    @Before
    public void before() {

    }

    @Test
    public void given_3_stored_access_objects_2_for_project1_1_for_project2_a_delete_all_for_project1_does_only_delete_project1_parts() throws Exception {
        /* prepare */
        ScanAccess access1 = new ScanAccess("user1", "project1");
        ScanAccess access2 = new ScanAccess("user2", "project1");
        ScanAccess access3 = new ScanAccess("user1", "project2");

        repository.save(access1);
        repository.save(access2);
        repository.save(access3);

        /* check preconditions */
        assertEquals(3, repository.count());
        assertNotNull(repository.findById(access3.getKey()));

        /* execute */
        repository.deleteAnyAccessForProject("project1");

        /* test */
        assertEquals(1, repository.count());
        assertNotNull(repository.findById(access3.getKey()));
    }

    @Test
    public void given_3_stored_access_objects_2_for_user1_1_for_user_2_a_delete_all_for_user1_does_only_delete_user1_parts() throws Exception {
        /* prepare */
        ScanAccess access1 = new ScanAccess("user1", "project1");
        ScanAccess access2 = new ScanAccess("user2", "project1");
        ScanAccess access3 = new ScanAccess("user1", "project2");

        repository.save(access1);
        repository.save(access2);
        repository.save(access3);

        /* check preconditions */
        assertEquals(3, repository.count());
        assertNotNull(repository.findById(access2.getKey()));

        /* execute */
        repository.deleteAcessForUserAtAll("user1");

        /* test */
        assertEquals(1, repository.count());
        assertNotNull(repository.findById(access2.getKey()));
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
