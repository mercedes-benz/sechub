// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.mercedesbenz.sechub.test.TestURLBuilder.*;
import static com.mercedesbenz.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Annotation;

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

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.project.ProjectJsonInput;
import com.mercedesbenz.sechub.domain.administration.project.ProjectJsonInput.ProjectWhiteList;
import com.mercedesbenz.sechub.domain.administration.project.ProjectRepository;
import com.mercedesbenz.sechub.domain.administration.project.ProjectUpdateAdministrationRestController;
import com.mercedesbenz.sechub.domain.administration.project.ProjectUpdateMetaDataEntityService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectUpdateWhitelistService;
import com.mercedesbenz.sechub.domain.administration.project.UpdateProjectInputValidator;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseUpdateProjectMetaData;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.project.UseCaseUpdateProjectWhitelist;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ProjectUpdateAdministrationRestController.class)
@ContextConfiguration(classes = { ProjectUpdateAdministrationRestController.class,
        ProjectUpdateAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(authorities = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
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
    @UseCaseRestDoc(useCase = UseCaseUpdateProjectWhitelist.class)
    public void restdoc_update_white_list_for_project() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUpdateProjectWhiteListUrl(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUpdateProjectWhitelist.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		    post(apiEndpoint, "projectId1").
        		    contentType(MediaType.APPLICATION_JSON_VALUE).
        		    content("{\"apiVersion\":\"1.0\", \"whiteList\":{\"uris\":[\"192.168.1.1\",\"https://my.special.server.com/myapp1/\"]}}")
        		).
        			andExpect(status().isOk()).
        			andDo(document(RestDocFactory.createPath(useCase),
                            resource(
                                    ResourceSnippetParameters.builder().
                                        summary(RestDocFactory.createSummary(useCase)).
                                        description(RestDocFactory.createDescription(useCase)).
                                        tag(RestDocFactory.extractTag(apiEndpoint)).
                                        requestSchema(OpenApiSchema.PROJECT_WHITELIST.getSchema()).
                                        pathParameters(
                                                parameterWithName(PROJECT_ID.paramName()).description("The id of the project for which whitelist shall be updated")
                                        ).
                                        requestFields(
                                                fieldWithPath(ProjectJsonInput.PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                                fieldWithPath(ProjectJsonInput.PROPERTY_WHITELIST+"."+ProjectWhiteList.PROPERTY_URIS).description("All URIS used now for whitelisting. Former parts will be replaced completely!")
                                        ).
                                        build()
                                     )
        			 ));

		/* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseUpdateProjectMetaData.class)
    public void restdoc_update_metadata_for_project() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUpdateProjectMetaData(PROJECT_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUpdateProjectMetaData.class;

        /* execute + test @formatter:off */
        this.mockMvc.perform(
        		post(apiEndpoint, "projectId1").
        				contentType(MediaType.APPLICATION_JSON_VALUE).
        				content("{\"apiVersion\":\"1.0\", \"metaData\":{\"key1\":\"value1\"}}")
        		).
        	    andExpect(status().isOk()).
        		andDo(document(RestDocFactory.createPath(useCase),
                        resource(
                                ResourceSnippetParameters.builder().
                                    summary(RestDocFactory.createSummary(useCase)).
                                    description(RestDocFactory.createDescription(useCase)).
                                    tag(RestDocFactory.extractTag(apiEndpoint)).
                                    requestSchema(OpenApiSchema.PROJECT_WHITELIST.getSchema()).
                                    pathParameters(
                                            parameterWithName(PROJECT_ID.paramName()).description("The id of the project for which metadata shall be updated")
                                    ).
                                    requestFields(
                                            fieldWithPath(ProjectJsonInput.PROPERTY_API_VERSION).description("The api version, currently only 1.0 is supported"),
                                            fieldWithPath(ProjectJsonInput.PROPERTY_METADATA).description("Metadata object. Contains key-value pairs."),
                                            fieldWithPath(ProjectJsonInput.PROPERTY_METADATA + ".key1").description("An arbitrary metadata key.")
                                    ).
                                    build()
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
