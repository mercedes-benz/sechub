// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.log;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.daimler.sechub.domain.scan.access.ScanAccessRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ScanAccessRepository.class, ProjectScanLogRepositoryDBTest.SimpleTestConfiguration.class })
public class ProjectScanLogRepositoryDBTest {

	@Autowired
	private ProjectScanLogRepository repository;

	@Before
	public void before() {

	}

	@Test
	public void given_3_stored_objects_2_for_project1_1_for_project2_a_delete_all_for_project1_does_only_delete_project1_parts() throws Exception {
		/* prepare */
		ProjectScanLog access1 = createNewProjectScanLog("project1");
		ProjectScanLog access2 = createNewProjectScanLog("project1");
		ProjectScanLog access3 = createNewProjectScanLog("project2");

		repository.save(access1);
		repository.save(access2);
		repository.save(access3);

		/* check preconditions */
		assertEquals(3, repository.count());
		assertNotNull(repository.findById(access3.getSechubJobUUID()));

		/* execute */
		repository.deleteAllLogDataForProject("project1");

		/* test */
		assertEquals(1, repository.count());
		assertNotNull(repository.findById(access3.getSechubJobUUID()));
	}

	private ProjectScanLog createNewProjectScanLog(String projectId) {
		return new ProjectScanLog(projectId, UUID.randomUUID(), "testuser", "{}");
	}

	@TestConfiguration
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration {

	}

}
