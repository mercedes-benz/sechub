package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.defineRestService;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.domain.administration.project.GetProjectsRestController;
import com.mercedesbenz.sechub.domain.administration.project.GetProjectsService;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.project.UseCaseGetProjects;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(GetProjectsRestController.class)
@ContextConfiguration(classes = { GetProjectsRestController.class, UserAdministrationRestControllerRestDocTest.SimpleTestConfiguration.class })
@WithMockUser(roles = RoleConstants.ROLE_USER)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class GetProjectsRestControllerTest {
    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetProjectsService getProjectsService;

    @MockBean
    private UserContextService userContextService;

    @Test
    @UseCaseRestDoc(useCase = UseCaseGetProjects.class)
    public void user_list_projects() throws Exception {
        /* prepare */
        String username = "user1";
        String apiEndpoint = https(PORT_USED).buildUserListProjectsUrl();
        Class<? extends Annotation> useCase = UseCaseGetProjects.class;

        List<String> projects = new ArrayList<>();
        projects.add("project1");
        String[] projectArray = projects.toArray(new String[0]);

        when(getProjectsService.userListProjects(username)).thenReturn(projectArray);

        when(userContextService.getUserId()).thenReturn(username);

        /* execute + test @formatter:off */
        this.mockMvc.perform(
                        get(apiEndpoint, "user1").
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
                                        fieldWithPath("[]").description("List of projects assigned to the user.")
                                )
                        )
                );

        /* @formatter:on */

    }
}