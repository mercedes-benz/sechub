// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.daimler.sechub.docgen.util.RestDocPathFactory;
import com.daimler.sechub.domain.administration.project.ProjectAdministrationRestController;
import com.daimler.sechub.domain.scan.log.ProjectScanLog;
import com.daimler.sechub.domain.scan.log.ProjectScanLogService;
import com.daimler.sechub.domain.scan.log.ProjectScanLogSummary;
import com.daimler.sechub.domain.scan.log.ScanLogRestController;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorShowsScanLogsForProject;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
@RunWith(SpringRunner.class)
@WebMvcTest(ProjectAdministrationRestController.class)
@ContextConfiguration(classes = { ScanLogRestController.class,
		AdminshowsScanLogsForProjectRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class AdminshowsScanLogsForProjectRestDocTest {

	private static final String PROJECT1 = "project1";

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	ProjectScanLogService projectScanLogService;

	@Before
	public void before() {
		List<ProjectScanLogSummary> summaries = new ArrayList<>();
		String status=ProjectScanLog.STATUS_OK;

		LocalDateTime started = LocalDateTime.now().minusDays(1);
		LocalDateTime ended = LocalDateTime.now();
		String executedBy = "spartakus";
		UUID sechubJobUUID= UUID.randomUUID();
		ProjectScanLogSummary summary = new ProjectScanLogSummary(sechubJobUUID, executedBy, started, ended, status);
		summaries.add(summary);

		when(projectScanLogService.fetchSummaryLogsFor(eq(PROJECT1))).thenReturn(summaries);
	}

	@Test
	@UseCaseRestDoc(useCase=UseCaseAdministratorShowsScanLogsForProject.class)
	public void restdoc_admin_downloads_scan_logs_for_project() throws Exception {

		/* execute + test @formatter:off */
		this.mockMvc.perform(
				get(https(PORT_USED).buildAdminFetchesScanLogsForProject(PROJECT1)).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				)./*andDo(print()).*/
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorShowsScanLogsForProject.class),
				/* we do not document more, because its binary / zip file...*/
					responseFields(
							 	fieldWithPath("[]").description("An array of scan log summary entries"),
							 	fieldWithPath("[].executedBy").description("The user id of the user which executed the scan"),
					            fieldWithPath("[].started").description("The timestamp when the scan was started"),
					            fieldWithPath("[].ended").description("The timestamp when the scan was ended"),
					            fieldWithPath("[].status").description("A status field about scan situation"),
					            fieldWithPath("[].sechubJobUUID").description("The uuid of corresponding sechub Job.")

							)
				)

				);

		/* @formatter:on */
	}

	@Profile(Profiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	}

}
