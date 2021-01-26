// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.whitelist;

import static com.daimler.sechub.sharedkernel.configuration.TestSecHubConfigurationBuilder.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class ProjectWhiteListSecHubConfigurationValidationServiceTest {

	private static final String IP_ADRESS_2 = "192.168.1.2";
	private static final String IP_ADRESS_1 = "192.168.1.1";
	private ProjectWhiteListSecHubConfigurationValidationService serviceToTest;
	private String projectId = "project1";

	@Rule
	public ExpectedException expectedException = ExpectedExceptionFactory.none();
	private ProjectWhiteListSupport support;
	private ProjectWhitelistEntryRepository repository;
	private List<ProjectWhitelistEntry> whiteListFoundForProject;


	@Before
	public void before() throws Exception {
		/* prepare */
		serviceToTest=new ProjectWhiteListSecHubConfigurationValidationService();
		support = mock(ProjectWhiteListSupport.class);

		repository=mock(ProjectWhitelistEntryRepository.class);
		whiteListFoundForProject=new ArrayList<>();

		serviceToTest.support=support;
		serviceToTest.projectWhiteListEntryRepository=repository;

		when(repository.fetchWhiteListEntriesForProject(projectId)).thenReturn(whiteListFoundForProject);

	}


	@Test
	public void no_configured_uris_no_call_to_validation_support() {
		/* execute */
		serviceToTest.assertAllowedForProject(configureSecHub().projectId(projectId).build());

		/* test */
		verifyNoInteractions(support);
	}


	@Test
	public void configured_webscan_uri_validation_support_called() throws Exception{
		/* prepare */
		whiteListFoundForProject.add(new ProjectWhitelistEntry(projectId, new URI(IP_ADRESS_1)));
		when(support.isWhitelisted(any(), any())).thenReturn(true); //always whitelisted

		/* execute */
		serviceToTest.assertAllowedForProject(configureSecHub().projectId(projectId).webConfig().addURI(IP_ADRESS_1).build());

		/* test */
		verify(support).isWhitelisted(eq(IP_ADRESS_1), any());
	}

	@Test
	public void when_support_says_webscan_uri_is_not_whitelisted_a_notacceptable_exception_is_thrown() throws Exception{
		/* test */
		expectedException.expect(NotAcceptableException.class);

		/* prepare */
		whiteListFoundForProject.add(new ProjectWhitelistEntry(projectId, new URI(IP_ADRESS_2)));
		when(support.isWhitelisted(any(), any())).thenReturn(false); // not whitelisted - so failure expected

		/* execute */
		serviceToTest.assertAllowedForProject(configureSecHub().projectId(projectId).webConfig().addURI(IP_ADRESS_1).build());

	}

	@Test
	public void configured_infrascan_uri_validation_support_called() throws Exception{
		/* prepare */
		whiteListFoundForProject.add(new ProjectWhitelistEntry(projectId, new URI(IP_ADRESS_1)));
		when(support.isWhitelisted(eq(IP_ADRESS_1), any())).thenReturn(true); //always whitelisted

		/* execute */
		serviceToTest.assertAllowedForProject(configureSecHub().projectId(projectId).infraConfig().addURI(IP_ADRESS_1).build());

		/* test */
		verify(support).isWhitelisted(eq(IP_ADRESS_1), any());
	}

	@Test
	public void when_support_says_infracan_uri_is_not_whitelisted_a_notacceptable_exception_is_thrown() throws Exception{
		/* test */
		expectedException.expect(NotAcceptableException.class);

		/* prepare */
		whiteListFoundForProject.add(new ProjectWhitelistEntry(projectId, new URI(IP_ADRESS_2)));
		when(support.isWhitelisted(eq(IP_ADRESS_1), any())).thenReturn(false); // not whitelisted - so failure expected

		/* execute */
		serviceToTest.assertAllowedForProject(configureSecHub().projectId(projectId).infraConfig().addURI(IP_ADRESS_1).build());

	}

	@Test
	public void configured_infrascan_ip_validation_support_called() throws Exception{
		/* prepare */
		whiteListFoundForProject.add(new ProjectWhitelistEntry(projectId, new URI(IP_ADRESS_1)));
		when(support.isWhitelisted(eq(IP_ADRESS_1), any())).thenReturn(true); //always whitelisted

		/* execute */
		serviceToTest.assertAllowedForProject(configureSecHub().projectId(projectId).infraConfig().addIP(IP_ADRESS_1).build());

		/* test */
		verify(support).isWhitelisted(eq(IP_ADRESS_1), any());
	}

	@Test
	public void when_support_says_infracan_ip_is_not_whitelisted_a_notacceptable_exception_is_thrown() throws Exception{
		/* test */
		expectedException.expect(NotAcceptableException.class);

		/* prepare */
		whiteListFoundForProject.add(new ProjectWhitelistEntry(projectId, new URI(IP_ADRESS_2)));
		when(support.isWhitelisted(eq(IP_ADRESS_1), any())).thenReturn(false); // not whitelisted - so failure expected

		/* execute */
		serviceToTest.assertAllowedForProject(configureSecHub().projectId(projectId).infraConfig().addIP(IP_ADRESS_1).build());

	}
}
