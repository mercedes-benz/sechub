// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentationTest.*;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.project.ProjectChangeOwnerService;
import com.mercedesbenz.sechub.domain.administration.project.ProjectManagementRestController;
import com.mercedesbenz.sechub.server.SecHubWebMvcConfigurer;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAdminOrOwnerChangesProjectOwner;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { ProjectManagementRestController.class, SecHubWebMvcConfigurer.class })
@Import(TestRestDocSecurityConfiguration.class)
@WithMockUser(roles = RoleConstants.ROLE_SUPERADMIN)
@ActiveProfiles({ Profiles.TEST, Profiles.ADMIN_ACCESS })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class ProjectManagementRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ProjectChangeOwnerService assignOwnerService;

    @Test
    @UseCaseRestDoc(useCase = UseCaseAdminOrOwnerChangesProjectOwner.class)
    public void restdoc_change_project_owner() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAdminChangesProjectOwnerUrl(PROJECT_ID.pathElement(), USER_ID.pathElement());
        Class<? extends Annotation> useCase = UseCaseAdminOrOwnerChangesProjectOwner.class;

        /* execute + test @formatter:off */
        mockMvc.perform(
                post(apiEndpoint, "projectId1", "userId1").
	                contentType(MediaType.APPLICATION_JSON_VALUE).
	                header(TestAuthenticationHelper.HEADER_NAME, TestAuthenticationHelper.getHeaderValue())
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
                                    parameterWithName(PROJECT_ID.paramName()).description("The project id"),
                                    parameterWithName(USER_ID.paramName()).description("The user id of the user to assign to project as the owner")
                            )
                ));
        /* @formatter:on */
    }
}