// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

public class ProjectUpdateMetaDataServiceTest {

	private ProjectUpdateMetaDataService serviceToTest;
	private ProjectRepository repository;
	private ProjectMetaDataRepository metaDataRepository;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private Set<ProjectMetaDataEntry> metaData = new HashSet<>();
	private Project project;
	
	private final String projectId = "projectid1";

	@Before
	public void before() throws Exception {
		serviceToTest = new ProjectUpdateMetaDataService();

		repository = mock(ProjectRepository.class);
		metaDataRepository = mock(ProjectMetaDataRepository.class);

		serviceToTest.repository = repository;
		serviceToTest.metaDataRepository = metaDataRepository;
		serviceToTest.auditLog = mock(AuditLogService.class);
		serviceToTest.assertion = mock(UserInputAssertion.class);
		serviceToTest.logSanitizer = mock(LogSanitizer.class);

		project = mock(Project.class);
				
		metaData.add(new ProjectMetaDataEntry(projectId, "key1", "value1"));
		metaData.add(new ProjectMetaDataEntry(projectId, "key2", "value2"));
		
		when(project.getMetaData()).thenReturn(metaData);
	}

	@Test
	public void project_not_found_throws_not_found_exception() {
		/* test */
		expectedException.expect(NotFoundException.class);

		/* prepare*/
		when(repository.findById(projectId)).thenReturn(Optional.empty());

		/* execute */
		serviceToTest.updateProjectMetaData(projectId, null);
	}

	@Test
	public void project_found__but_metadata_empty_updates_with_empty_list() {
		/* prepare*/
		when(repository.findById(projectId)).thenReturn(Optional.of(project));

		List<ProjectMetaData> emptyMetaDataList = Collections.emptyList();
		List<ProjectMetaDataEntry> emptyMetaDataEntryList = Collections.emptyList();
		
		/* execute */
		serviceToTest.updateProjectMetaData(projectId, emptyMetaDataList);

		/* test */
		verify(metaDataRepository).deleteAll(metaData);
		verify(metaDataRepository).saveAll(emptyMetaDataEntryList);
	}

	@Test
	public void project_found__and_2_metadata_entries_updated() throws Exception{
		/* prepare*/
		when(repository.findById(projectId)).thenReturn(Optional.of(project));

		List<ProjectMetaData> newMetaDataList = Arrays.asList( new ProjectMetaData("key3", "value3"), new ProjectMetaData("key4", "value4"));
		List<ProjectMetaDataEntry> newMetaDataEntryList = Arrays.asList( new ProjectMetaDataEntry(projectId, "key3", "value3"), new ProjectMetaDataEntry(projectId, "key4", "value4"));
		
		/* execute */
		serviceToTest.updateProjectMetaData(projectId, newMetaDataList);
		/* test */
		verify(metaDataRepository).deleteAll(metaData);
		verify(metaDataRepository).saveAll(newMetaDataEntryList);
	}
}
