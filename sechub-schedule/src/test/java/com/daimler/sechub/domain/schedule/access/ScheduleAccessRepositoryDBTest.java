// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.access;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes= {JobRepository.class, ScheduleAccessRepositoryDBTest.SimpleTestConfiguration.class})
public class ScheduleAccessRepositoryDBTest {

	@Autowired
	private ScheduleAccessRepository jobRepository;

	@Before
	public void before() {

	}

	@Test
	public void given_3_stored_access_objects_2_for_project1_1_for_project2_a_delete_all_for_project1_does_only_delete_project1_parts() throws Exception {
		/* prepare */
		ScheduleAccess access1 = new ScheduleAccess("user1","project1");
		ScheduleAccess access2 = new ScheduleAccess("user2","project1");
		ScheduleAccess access3 = new ScheduleAccess("user1","project2");

		jobRepository.save(access1);
		jobRepository.save(access2);
		jobRepository.save(access3);

		/* check preconditions*/
		assertEquals(3, jobRepository.count());
		assertNotNull(jobRepository.findById(access3.getKey()));

		/* execute */
		jobRepository.deleteAnyAccessForProject("project1");

		/* test */
		assertEquals(1, jobRepository.count());
		assertNotNull(jobRepository.findById(access3.getKey()));
	}

	@Test
	public void given_3_stored_access_objects_2_for_user1_1_for_user_2_a_delete_all_for_user1_does_only_delete_user1_parts() throws Exception {
		/* prepare */
		ScheduleAccess access1 = new ScheduleAccess("user1","project1");
		ScheduleAccess access2 = new ScheduleAccess("user2","project1");
		ScheduleAccess access3 = new ScheduleAccess("user1","project2");

		jobRepository.save(access1);
		jobRepository.save(access2);
		jobRepository.save(access3);

		/* check preconditions*/
		assertEquals(3, jobRepository.count());
		assertNotNull(jobRepository.findById(access2.getKey()));

		/* execute */
		jobRepository.deleteAcessForUserAtAll("user1");

		/* test */
		assertEquals(1, jobRepository.count());
		assertNotNull(jobRepository.findById(access2.getKey()));
	}



	@TestConfiguration
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration{

	}
}
