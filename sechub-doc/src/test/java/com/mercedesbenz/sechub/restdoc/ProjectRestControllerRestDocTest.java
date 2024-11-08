// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.defineRestService;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.project.ProjectData;
import com.mercedesbenz.sechub.domain.administration.project.ProjectRestController;
import com.mercedesbenz.sechub.domain.administration.project.ProjectService;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.project.UseCaseGetAssignedProjectDataList;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(ProjectRestController.class)
@ContextConfiguration(classes = { ProjectRestController.class, ProjectRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(roles = RoleConstants.ROLE_USER)
@ExtendWith(RestDocumentationExtension.class)
@ActiveProfiles({ Profiles.TEST })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class ProjectRestControllerRestDocTest implements TestIsNecessaryForDocumentation {
    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserContextService userContextService;

    @Test
    @UseCaseRestDoc(useCase = UseCaseGetAssignedProjectDataList.class)
    public void user_role_get_projects() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildGetProjects();
        Class<? extends Annotation> useCase = UseCaseGetAssignedProjectDataList.class;

        String username = "user1";

        ProjectData projectData = new ProjectData();
        projectData.setProjectId("project1");
        projectData.setOwner(username);
        projectData.setOwned(true);
        String[] assignedUsers = new String[] { "user1", "user2" };
        projectData.setAssignedUsers(assignedUsers);
        List<ProjectData> projectDataList = List.of(projectData);

        when(userContextService.getUserId()).thenReturn(username);
        when(projectService.getAssignedProjectDataList(username)).thenReturn(projectDataList);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                            get(apiEndpoint).
                            contentType(MediaType.APPLICATION_JSON_VALUE).
                            header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
                andExpect(status().isOk()).
                andDo(defineRestService().
                        with().
                        useCaseData(useCase).
                        tag(RestDocFactory.extractTag(apiEndpoint)).

                        and().
                        document(
                                requestHeaders(

                                ),
                                pathParameters(

                                ),
                                responseFields(
                                        fieldWithPath("[]." + ProjectData.PROPERTY_PROJECT_ID).description("Project ID"),
                                        fieldWithPath("[]." + ProjectData.PROPERTY_OWNER).description("Name of owner of the Project"),
                                        fieldWithPath("[]." + ProjectData.PROPERTY_IS_OWNED).description("If caller is owner of the project"),
                                        fieldWithPath("[]." + ProjectData.PROPERTY_ASSIGNED_USERS).description("Optional: Assigned users (only viewable by owner or admin)")
                                )
                        )
                );
        /* @formatter:on */

    }

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }
}