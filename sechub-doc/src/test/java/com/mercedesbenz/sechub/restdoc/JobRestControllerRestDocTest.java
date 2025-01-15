// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.defineRestService;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.JOB_UUID;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Annotation;
import java.util.UUID;

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
import com.mercedesbenz.sechub.domain.administration.job.JobCancelService;
import com.mercedesbenz.sechub.domain.administration.job.JobRestController;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.RoleConstants;
import com.mercedesbenz.sechub.sharedkernel.security.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.job.UseCaseUserCancelsJob;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { JobRestController.class })
@Import(TestRestDocSecurityConfiguration.class)
@WithMockUser(roles = RoleConstants.ROLE_USER)
@ActiveProfiles({ Profiles.TEST })
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class JobRestControllerRestDocTest implements TestIsNecessaryForDocumentation {
    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobCancelService jobCancelService;

    @MockBean
    private UserContextService userContextService;

    @Test
    @UseCaseRestDoc(useCase = UseCaseUserCancelsJob.class)
    public void user_role_cancel_job() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUserCancelJob(JOB_UUID.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserCancelsJob.class;

        /* execute + test @formatter:off */
        UUID jobUUID = UUID.randomUUID();
        this.mockMvc.perform(
                        post(apiEndpoint, jobUUID).
                                contentType(MediaType.APPLICATION_JSON_VALUE).
                                header(AuthenticationHelper.HEADER_NAME, AuthenticationHelper.getHeaderValue())
                ).
                andExpect(status().isNoContent()).
                andDo(defineRestService().
                        with().
                        useCaseData(useCase).
                        tag(RestDocFactory.extractTag(apiEndpoint)).
                        and().
                        document(
                                requestHeaders(

                                ),
                                pathParameters(
                                        parameterWithName(JOB_UUID.paramName()).description("The job UUID")
                                )
                        ));

        /* @formatter:on */
    }
}
