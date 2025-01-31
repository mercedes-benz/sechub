package com.mercedesbenz.sechub.domain.administration.user;

import static com.mercedesbenz.sechub.test.RestDocPathParameter.EMAIL_ADDRESS;
import static com.mercedesbenz.sechub.test.SecHubTestURLBuilder.https;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.test.TestPortProvider;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { AnonymousUserRestController.class })
@ActiveProfiles({ Profiles.TEST })
public class AnonymousUserRestControllerTest {

    private static final int PORT_USED = TestPortProvider.DEFAULT_INSTANCE.getWebMVCTestHTTPSPort();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserEmailAddressUpdateService userEmailAddressUpdateService;

    @Test
    void verifyEmailAddress() throws Exception {
        /* prepare */
        String token = "token1";
        doNothing().when(userEmailAddressUpdateService).userVerifiesUserEmailAddress(EMAIL_ADDRESS.pathElement());

        /* @formatter:off */
        /* execute + test */
        this.mockMvc.perform(
                get(https(PORT_USED).buildUnauthenticatedUserVerifyEmailAddressUrl(token)))
                .andExpect(status().isOk());
    }

}