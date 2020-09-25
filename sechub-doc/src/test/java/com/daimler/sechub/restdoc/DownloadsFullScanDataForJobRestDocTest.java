// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.daimler.sechub.domain.scan.admin.FullScanData;
import com.daimler.sechub.domain.scan.admin.FullScanDataRestController;
import com.daimler.sechub.domain.scan.admin.FullScanDataService;
import com.daimler.sechub.domain.scan.admin.ScanData;
import com.daimler.sechub.domain.scan.log.ProjectScanLog;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.logging.AuditLogService;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseAdministratorDownloadsFullScanDataForJob;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;
@RunWith(SpringRunner.class)
@WebMvcTest(ProjectAdministrationRestController.class)
@ContextConfiguration(classes = { FullScanDataRestController.class,
		DownloadsFullScanDataForJobRestDocTest.SimpleTestConfiguration.class,LogSanitizer.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class DownloadsFullScanDataForJobRestDocTest {

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	FullScanDataService fullScanDataService;

	@MockBean
	AuditLogService auditLogService;

	private UUID sechubJobUUID;

	@Before
	public void before() {
		sechubJobUUID = UUID.randomUUID();
		FullScanData data = new FullScanData();
		ScanData d = new ScanData();
		d.productId="productX";
		d.result="{ 'result':'OK'}";
		d.metaData="{}";
		data.allScanData.add(d);

		String config="{}";
		ProjectScanLog log =new ProjectScanLog("theProject", sechubJobUUID, "spartakus", config);
		data.allScanLogs.add(log);

		when(fullScanDataService.getFullScanData(sechubJobUUID)).thenReturn(data);
	}

	@Test
	@UseCaseRestDoc(useCase=UseCaseAdministratorDownloadsFullScanDataForJob.class)
	public void restdoc_admin_downloads_fullscan_data_for_job() throws Exception {


		/* execute + test @formatter:off */
		this.mockMvc.perform(
				get(https(PORT_USED).buildAdminDownloadsZipFileContainingFullScanDataFor(sechubJobUUID)).
				contentType(MediaType.APPLICATION_JSON_VALUE)
				)./*andDo(print()).*/
		andExpect(status().isOk()).
		andDo(document(RestDocPathFactory.createPath(UseCaseAdministratorDownloadsFullScanDataForJob.class)));

		/* @formatter:on */
	}

	@Profile(Profiles.TEST)
	@EnableAutoConfiguration
	public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

	}

}
