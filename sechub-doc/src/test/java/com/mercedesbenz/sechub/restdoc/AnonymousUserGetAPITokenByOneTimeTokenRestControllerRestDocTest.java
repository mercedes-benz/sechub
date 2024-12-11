// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.defineRestService;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.ONE_TIME_TOKEN;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.user.AnonymousUserGetAPITokenByOneTimeTokenService;
import com.mercedesbenz.sechub.domain.administration.user.AnonymousUserGetApiTokenByOneTimeTokenRestController;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserClicksLinkToGetNewAPIToken;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@ContextConfiguration(classes = { AnonymousUserGetApiTokenByOneTimeTokenRestController.class })
@Import(TestRestDocSecurityConfiguration.class)
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class AnonymousUserGetAPITokenByOneTimeTokenRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AnonymousUserGetAPITokenByOneTimeTokenService userApiTokenService;

    @BeforeEach
    public void beforeEach() {
    }

    @Test
    @WithAnonymousUser
    @UseCaseRestDoc(useCase = UseCaseUserClicksLinkToGetNewAPIToken.class)
    public void restdoc_user_clicks_link_to_get_NewApiToken() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildAnonymousGetNewApiTokenByLinkWithOneTimeTokenUrl(ONE_TIME_TOKEN.pathElement());
        Class<? extends Annotation> useCase = UseCaseUserClicksLinkToGetNewAPIToken.class;

        /* execute + test @formatter:off */
		this.mockMvc.perform(
				get(apiEndpoint,"oneTimeToken1").
				contentType(MediaType.APPLICATION_JSON_VALUE)
				).
		andExpect(status().isOk()).
		andDo(defineRestService().
                with().
                    useCaseData(useCase).
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                and().
                document(
                            pathParameters(
                                    parameterWithName(ONE_TIME_TOKEN.paramName()).description("A one time token the user has got by a previous mail from sechub server")
                         )
				));

		/* @formatter:on */
    }

}
