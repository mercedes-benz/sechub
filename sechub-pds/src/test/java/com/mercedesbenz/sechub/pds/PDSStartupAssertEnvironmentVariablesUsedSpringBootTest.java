package com.mercedesbenz.sechub.pds;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.mercedesbenz.sechub.pds.commons.core.PDSProfiles;

@SpringBootTest(classes = { PDSStartupAssertEnvironmentVariablesUsed.class })
@ActiveProfiles(PDSProfiles.PROD)
class PDSStartupAssertEnvironmentVariablesUsedSpringBootTest {

    @MockBean
    PDSStartupAssertEnvironmentVariablesUsed assertionToTest;

    @Test
    void in_prod_profile_assertOnApplicationStart_is_called() {
        verify(assertionToTest).assertOnApplicationStart();
    }

}
