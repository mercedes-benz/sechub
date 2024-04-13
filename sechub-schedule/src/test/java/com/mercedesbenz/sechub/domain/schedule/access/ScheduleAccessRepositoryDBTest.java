// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.access;

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
@ContextConfiguration(classes = { ScheduleAccessRepository.class, ScheduleAccessRepositoryDBTest.SimpleTestConfiguration.class })
public class ScheduleAccessRepositoryDBTest {

    @Autowired
    private ScheduleAccessRepository repository;

    @Before
    public void before() {

    }

    @Test
    public void given_1_stored_access_and_one_unknown_project_check_for_existing_user_access() {
        /* prepare */
        String project1 = "project1";
        String project2 = "project2";

        ScheduleAccess access1 = new ScheduleAccess("user1", project1);

        repository.save(access1);

        /* check preconditions */
        assertEquals(1, repository.count());
        assertNotNull(repository.findById(access1.getKey()));

        /* execute & test */
        assertTrue(repository.hasProjectUserAccess(project1));
        assertFalse(repository.hasProjectUserAccess(project2));
    }

    @Test
    public void given_3_stored_access_objects_2_for_project1_1_for_project2_a_delete_all_for_project1_does_only_delete_project1_parts() throws Exception {
        /* prepare */
        ScheduleAccess access1 = new ScheduleAccess("user1", "project1");
        ScheduleAccess access2 = new ScheduleAccess("user2", "project1");
        ScheduleAccess access3 = new ScheduleAccess("user1", "project2");

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
        ScheduleAccess access1 = new ScheduleAccess("user1", "project1");
        ScheduleAccess access2 = new ScheduleAccess("user2", "project1");
        ScheduleAccess access3 = new ScheduleAccess("user1", "project2");

        repository.save(access1);
        repository.save(access2);
        repository.save(access3);

        /* check preconditions */
        assertEquals(3, repository.count());
        assertNotNull(repository.findById(access2.getKey()));

        /* execute */
        repository.deleteAccessForUserAtAll("user1");

        /* test */
        assertEquals(1, repository.count());
        assertNotNull(repository.findById(access2.getKey()));
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }
}
