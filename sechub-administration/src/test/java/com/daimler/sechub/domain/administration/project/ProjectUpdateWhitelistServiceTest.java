// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

import com.daimler.sechub.sharedkernel.UserContextService;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.messaging.DomainMessageService;
import com.daimler.sechub.sharedkernel.validation.URIValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

public class ProjectUpdateWhitelistServiceTest {

	private ProjectUpdateWhitelistService serviceToTest;
	private ProjectRepository repository;
	private UserContextService userContext;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private Set<URI> whitelist;
	private Project project;
	private DomainMessageService eventBus;
	private URIValidation uriValidation;

	@Before
	public void before() throws Exception {
		serviceToTest = new ProjectUpdateWhitelistService();

		repository = mock(ProjectRepository.class);
		userContext = mock(UserContextService.class);
		eventBus = mock(DomainMessageService.class);
		uriValidation = mock(URIValidation.class);

		serviceToTest.repository=repository;
		serviceToTest.userContext=userContext;
		serviceToTest.eventBus=eventBus;
		serviceToTest.uriValidation=uriValidation;

		project = mock(Project.class);
		whitelist=new LinkedHashSet<>();
		whitelist.add(new URI("127.0.0.1"));
		when(project.getWhiteList()).thenReturn(whitelist);
		when(uriValidation.validate(any())).thenReturn(new ValidationResult());
	}


	@Test
	public void project_not_found_throws_not_found_exception() {
		/* test */
		expectedException.expect(NotFoundException.class);

		/* prepare*/
		when(repository.findById("projectId")).thenReturn(Optional.empty());

		/* execute */
		serviceToTest.updateProjectWhitelist("projectId", null);

	}

	@Test
	public void project_found__but_uris_empty_updates_with_empty_list() {
		/* prepare*/
		when(repository.findById("projectId")).thenReturn(Optional.of(project));

		/* execute */
		serviceToTest.updateProjectWhitelist("projectId", Collections.emptyList());

		/* test */
		verify(repository).save(project);
		assertEquals(0,whitelist.size());
	}

	@Test
	public void project_found__and_2_uris_contained_updates_to_those_uris() throws Exception{
		/* prepare*/
		URI uri1 = new URI("http://www.example.org");
		URI uri2 = new URI("http://www.example.com");
		when(repository.findById("projectId")).thenReturn(Optional.of(project));
		List<URI> uris = Arrays.asList(uri1,uri2);

		/* execute */
		serviceToTest.updateProjectWhitelist("projectId", uris);

		/* test */
		verify(repository).save(project);

		assertEquals(2,whitelist.size());
		assertTrue(whitelist.contains(uri1));
		assertTrue(whitelist.contains(uri2));
	}

}
