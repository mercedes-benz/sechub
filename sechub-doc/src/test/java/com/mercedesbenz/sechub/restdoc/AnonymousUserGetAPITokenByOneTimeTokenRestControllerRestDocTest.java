// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.TestURLBuilder.*;
import static com.mercedesbenz.sechub.test.TestURLBuilder.RestDocPathParameter.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.user.AnonymousUserGetAPITokenByOneTimeTokenService;
import com.mercedesbenz.sechub.domain.administration.user.AnonymousUserGetApiTokenByOneTimeTokenRestController;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.user.UseCaseUserClicksLinkToGetNewAPIToken;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest(AnonymousUserGetApiTokenByOneTimeTokenRestController.class)
@ContextConfiguration(classes = { AnonymousUserGetApiTokenByOneTimeTokenRestController.class,
        AnonymousUserGetAPITokenByOneTimeTokenRestControllerRestDocTest.SimpleTestConfiguration.class })
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class AnonymousUserGetAPITokenByOneTimeTokenRestControllerRestDocTest implements TestIsNecessaryForDocumentation {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AnonymousUserGetAPITokenByOneTimeTokenService userApiTokenService;

    @Before
    public void before() {
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

    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }

}
