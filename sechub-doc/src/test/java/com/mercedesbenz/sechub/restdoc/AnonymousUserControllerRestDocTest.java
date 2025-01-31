package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentationTest.defineRestService;
import static com.mercedesbenz.sechub.test.RestDocPathParameter.ONE_TIME_TOKEN;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.annotation.Annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.domain.administration.user.AnonymousUserRestController;
import com.mercedesbenz.sechub.domain.administration.user.UserEmailAddressUpdateService;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.admin.user.UseCaseAnonymousUserVerifiesEmailAddress;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { AnonymousUserRestController.class })
@Import(TestRestDocSecurityConfiguration.class)
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class AnonymousUserControllerRestDocTest implements TestIsNecessaryForDocumentation {
    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserEmailAddressUpdateService emailAddressUpdateService;

    @Test
    @UseCaseRestDoc(useCase = UseCaseAnonymousUserVerifiesEmailAddress.class)
    public void restDoc__anonymousUserVerifiesEmailAddress() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildUnauthenticatedUserVerifyEmailAddressUrl(ONE_TIME_TOKEN.pathElement());
        Class<? extends Annotation> useCase = UseCaseAnonymousUserVerifiesEmailAddress.class;

        /* @formatter:off */
        /* execute + test */
        this.mockMvc.perform(
                get(apiEndpoint,"token1"))
                .andExpect(status().isOk())
                .andDo(defineRestService()
                        .with()
                        .useCaseData(useCase)
                        .tag(RestDocFactory.extractTag(apiEndpoint))
                        .and()
                        .document());
    }
    /* @formatter:on */

}
