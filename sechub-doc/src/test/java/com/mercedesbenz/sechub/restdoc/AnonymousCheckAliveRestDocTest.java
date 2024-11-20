// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.restdoc;

import static com.mercedesbenz.sechub.restdoc.RestDocumentation.*;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.docgen.util.RestDocFactory;
import com.mercedesbenz.sechub.server.core.AnonymousCheckAliveRestController;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.usecases.UseCaseRestDoc;
import com.mercedesbenz.sechub.sharedkernel.usecases.anonymous.UseCaseAnonymousCheckAlive;
import com.mercedesbenz.sechub.test.ExampleConstants;
import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { AnonymousCheckAliveRestController.class })
@Import(TestRestDocSecurityConfiguration.class)
@WithMockUser
@ActiveProfiles(Profiles.TEST)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = ExampleConstants.URI_SECHUB_SERVER, uriPort = 443)
public class AnonymousCheckAliveRestDocTest implements TestIsNecessaryForDocumentation {
    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getRestDocTestPort();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @UseCaseRestDoc(useCase = UseCaseAnonymousCheckAlive.class, variant = "HEAD")
    public void calling_check_alive_head_returns_HTTP_200() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildCheckIsAliveUrl();
        Class<? extends Annotation> useCase = UseCaseAnonymousCheckAlive.class;

        /* execute */
        /* @formatter:off */
        this.mockMvc.perform(
        			head(apiEndpoint)
        		).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                    useCaseData(useCase, "HEAD").
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                and().
                document()
        );

        /* @formatter:on */
    }

    @Test
    @UseCaseRestDoc(useCase = UseCaseAnonymousCheckAlive.class, variant = "GET")
    public void calling_check_alive_get_returns_HTTP_200() throws Exception {
        /* prepare */
        String apiEndpoint = https(PORT_USED).buildCheckIsAliveUrl();
        Class<? extends Annotation> useCase = UseCaseAnonymousCheckAlive.class;

        /* execute */
        /* @formatter:off */
        this.mockMvc.perform(
        			get(apiEndpoint)
        		).
        andExpect(status().isOk()).
        andDo(defineRestService().
                with().
                    useCaseData(useCase, "GET").
                    tag(RestDocFactory.extractTag(apiEndpoint)).
                and().
                document()
        );

        /* @formatter:on */
    }
}
