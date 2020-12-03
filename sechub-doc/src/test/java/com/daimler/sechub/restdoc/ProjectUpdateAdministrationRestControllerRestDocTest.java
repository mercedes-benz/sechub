// SPDX-License-Identifier: MIT
package com.daimler.sechub.restdoc;

import static com.daimler.sechub.test.TestURLBuilder.*;
import static com.daimler.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.daimler.sechub.domain.administration.project.ProjectJsonInput;
import com.daimler.sechub.domain.administration.project.ProjectJsonInput.ProjectWhiteList;
import com.daimler.sechub.domain.administration.project.ProjectRepository;
import com.daimler.sechub.domain.administration.project.ProjectUpdateAdministrationRestController;
import com.daimler.sechub.domain.administration.project.ProjectUpdateMetaDataEntityService;
import com.daimler.sechub.domain.administration.project.ProjectUpdateWhitelistService;
import com.daimler.sechub.domain.administration.project.UpdateProjectInputValidator;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.RoleConstants;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseUpdateProjectMetaData;
import com.daimler.sechub.sharedkernel.usecases.admin.project.UseCaseUpdateProjectWhitelist;
import com.daimler.sechub.test.ExampleConstants;
import com.daimler.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ProjectUpdateAdministrationRestController.class)
@ContextConfiguration(classes = { ProjectUpdateAdministrationRestController.class,
		ProjectUpdateAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({Profiles.TEST, Profiles.ADMIN_ACCESS})
@AutoConfigureRestDocs(uriScheme="https",uriHost=ExampleConstants.URI_SECHUB_SERVER,uriPort=443)
public class ProjectUpdateAdministrationRestControllerRestDocTest {

	private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	ProjectUpdateWhitelistService mockedProjectUpdateWhiteListService;
	
	@MockBean
	ProjectUpdateMetaDataEntityService mockedProjectUpdateMetaDataService;

	@MockBean
	UpdateProjectInputValidator mockedValidator;

	@MockBean
	ProjectRepository mockedProjectRepository;

	@Before
	public void before() {
		when(mockedValidator.supports(ProjectJsonInput.class)).thenReturn(true);
	}

	@Test
	@UseCaseRestDoc(useCase=UseCaseUpdateProjectWhitelist.class)
	public void restdoc_update_white_list_for_project() throws Exception {

		/* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).
        				buildUpdateProjectWhiteListUrl(PROJECT_ID.pathElement()),"projectId1").
        				contentType(MediaType.APPLICATION_JSON_VALUE).
        				content("{\"apiVersion\":\"1.0\", \"whiteList\":{\"uris\":[\"192.168.1.1\",\"https://my.special.server.com/myapp1/\"]}}")
        		).
        			andExpect(status().isOk()).
        			andDo(
        				document(RestDocPathFactory.createPath(UseCaseUpdateProjectWhitelist.class),
	        				pathParameters(
								parameterWithName(PROJECT_ID.paramName()).description("The id of the project for which whitelist shall be updated")
							),
	    					requestFields(
	    						fieldWithPath(ProjectJsonInput.PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
								fieldWithPath(ProjectJsonInput.PROPERTY_WHITELIST+"."+ProjectWhiteList.PROPERTY_URIS).description("All URIS used now for whitelisting. Former parts will be replaced completely!")
							)
        				)
        			);

		/* @formatter:on */
	}
	
	@Test
	@UseCaseRestDoc(useCase=UseCaseUpdateProjectMetaData.class)
	public void restdoc_update_metadata_for_project() throws Exception {

		/* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(https(PORT_USED).
        				buildUpdateProjectMetaData(PROJECT_ID.pathElement()),"projectId1").
        				contentType(MediaType.APPLICATION_JSON_VALUE).
        				content("{\"apiVersion\":\"1.0\", \"metaData\":{\"key1\":\"value1\"}}")
        		).
        			andExpect(status().isOk()).
        			andDo(
        				document(RestDocPathFactory.createPath(UseCaseUpdateProjectMetaData.class),
	        				pathParameters(
	        					parameterWithName(PROJECT_ID.paramName()).description("The id of the project for which metadata shall be updated")
	        				),
	        				requestFields(
								fieldWithPath(ProjectJsonInput.PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
								fieldWithPath(ProjectJsonInput.PROPERTY_METADATA).description("Metadata object. Contains key-value pairs."),
								fieldWithPath(ProjectJsonInput.PROPERTY_METADATA + ".key1").description("An arbitrary metadata key.")
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
