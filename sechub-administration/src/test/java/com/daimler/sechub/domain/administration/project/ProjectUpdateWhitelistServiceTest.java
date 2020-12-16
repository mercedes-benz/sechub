// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
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
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.validation.URIValidation;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class ProjectUpdateWhitelistServiceTest {

	private ProjectUpdateWhitelistService serviceToTest;
	private ProjectRepository repository;

	@Rule
	public ExpectedException expectedException = ExpectedExceptionFactory.none();

	private Set<URI> whitelist;
	private Project project;
	private DomainMessageService eventBus;
	private URIValidation uriValidation;
	
	private final String projectId = "projectId";

	@Before
	public void before() throws Exception {
		serviceToTest = new ProjectUpdateWhitelistService();

		repository = mock(ProjectRepository.class);
		eventBus = mock(DomainMessageService.class);
		uriValidation = mock(URIValidation.class);

		serviceToTest.repository = repository;
		serviceToTest.auditLog = mock(AuditLogService.class);
		serviceToTest.assertion = mock(UserInputAssertion.class);
		serviceToTest.eventBus = eventBus;
		serviceToTest.uriValidation = uriValidation;
		serviceToTest.logSanitizer = mock(LogSanitizer.class);

		project = mock(Project.class);
		whitelist = new LinkedHashSet<>();
		whitelist.add(new URI("127.0.0.1"));
		when(project.getWhiteList()).thenReturn(whitelist);
		when(uriValidation.validate(any())).thenReturn(new ValidationResult());
	}


	@Test
	public void project_not_found_throws_not_found_exception() {
		/* test */
		expectedException.expect(NotFoundException.class);

		/* prepare*/
		when(repository.findById(projectId)).thenReturn(Optional.empty());

		/* execute */
		serviceToTest.updateProjectWhitelist(projectId, null);

	}

	@Test
	public void project_found__but_uris_empty_updates_with_empty_list() {
		/* prepare*/
		when(repository.findById(projectId)).thenReturn(Optional.of(project));

		/* execute */
		serviceToTest.updateProjectWhitelist(projectId, Collections.emptyList());

		/* test */
		verify(repository).save(project);
		assertEquals(0,whitelist.size());
	}

	@Test
	public void project_found__and_2_uris_contained_updates_to_those_uris() throws Exception{
		/* prepare*/
		URI uri1 = new URI("http://www.example.org");
		URI uri2 = new URI("http://www.example.com");
		when(repository.findById(projectId)).thenReturn(Optional.of(project));
		List<URI> uris = Arrays.asList(uri1,uri2);

		/* execute */
		serviceToTest.updateProjectWhitelist(projectId, uris);

		/* test */
		verify(repository).save(project);

		assertEquals(2,whitelist.size());
		assertTrue(whitelist.contains(uri1));
		assertTrue(whitelist.contains(uri2));
	}

}
