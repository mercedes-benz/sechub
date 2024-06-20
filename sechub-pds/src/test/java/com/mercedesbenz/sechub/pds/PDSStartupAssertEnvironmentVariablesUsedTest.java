package com.mercedesbenz.sechub.pds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.pds.security.PDSSecurityConfiguration;
import com.mercedesbenz.sechub.pds.storage.PDSS3PropertiesSetup;
import com.mercedesbenz.sechub.pds.storage.PDSSharedVolumePropertiesSetup;

class PDSStartupAssertEnvironmentVariablesUsedTest {

    private PDSStartupAssertEnvironmentVariablesUsed assertionToTest;
    private SystemEnvironmentVariableSupport envVariableSupport;
    private PDSS3PropertiesSetup s3setup;
    private PDSSecurityConfiguration securityConfiguration;
    private PDSSharedVolumePropertiesSetup sharedVolumeSetup;

    @BeforeEach
    void beforeEach() {
        assertionToTest = new PDSStartupAssertEnvironmentVariablesUsed();

        s3setup = mock(PDSS3PropertiesSetup.class);
        securityConfiguration = mock(PDSSecurityConfiguration.class);
        envVariableSupport = mock(SystemEnvironmentVariableSupport.class);
        sharedVolumeSetup = mock(PDSSharedVolumePropertiesSetup.class);

        assertionToTest.envVariableSupport = envVariableSupport;
        assertionToTest.securityConfiguration = securityConfiguration;
        assertionToTest.s3Setup = s3setup;
        assertionToTest.sharedVolumeSetup = sharedVolumeSetup;

    }

    @Test
    void assertOnApplicationStart_when_value_support_says_environment_variable_has_same_value_no_exception() {
        /* prepare */
        when(envVariableSupport.isValueLikeEnvironmentVariableValue(any(), any())).thenReturn(true);

        /* execute + test */
        assertDoesNotThrow(() -> assertionToTest.assertOnApplicationStart());

    }

    @Test
    void assertOnApplicationStart_does_call_registration_methods_with_same_registry() {
        /* prepare */
        when(envVariableSupport.isValueLikeEnvironmentVariableValue(any(), any())).thenReturn(true);

        /* execute */
        assertionToTest.assertOnApplicationStart();

        /* test */
        ArgumentCaptor<SecureEnvironmentVariableKeyValueRegistry> captor = ArgumentCaptor.forClass(SecureEnvironmentVariableKeyValueRegistry.class);
        verify(s3setup).registerOnlyAllowedAsEnvironmentVariables(captor.capture());
        verify(sharedVolumeSetup).registerOnlyAllowedAsEnvironmentVariables(captor.capture());
        verify(securityConfiguration).registerOnlyAllowedAsEnvironmentVariables(captor.capture());

        List<SecureEnvironmentVariableKeyValueRegistry> registries = captor.getAllValues();
        assertEquals(3, registries.size());
        Iterator<SecureEnvironmentVariableKeyValueRegistry> it = registries.iterator();
        SecureEnvironmentVariableKeyValueRegistry registry0 = it.next();
        SecureEnvironmentVariableKeyValueRegistry registry1 = it.next();
        SecureEnvironmentVariableKeyValueRegistry registry2 = it.next();

        assertSame(registry0, registry1);
        assertSame(registry0, registry2);
        assertSame(registry1, registry2);

    }

    @Test
    void assertOnApplicationStart_does_use_registry_information_for_check() {
        /* prepare */
        when(envVariableSupport.isValueLikeEnvironmentVariableValue(any(), any())).thenReturn(true);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                SecureEnvironmentVariableKeyValueRegistry arg = (SecureEnvironmentVariableKeyValueRegistry) invocation.getArgument(0);
                arg.register(arg.newEntry().key("test.key1").variable("TEST_KEY_VARIABLE").value("val1"));
                return null;
            }

        }).when(s3setup).registerOnlyAllowedAsEnvironmentVariables(any(SecureEnvironmentVariableKeyValueRegistry.class));

        /* execute */
        assertionToTest.assertOnApplicationStart();

        /* test */

        // check verification is done with registry data
        verify(envVariableSupport).isValueLikeEnvironmentVariableValue("TEST_KEY_VARIABLE", "val1");

    }

    @Test
    void assertOnApplicationStart_fails_with_info__when_spring_data_source_is_not_env() {
        /* prepare */
        when(envVariableSupport.isValueLikeEnvironmentVariableValue(any(), any())).thenReturn(false);

        /* execute */
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> assertionToTest.assertOnApplicationStart());

        /* test */
        String message = thrown.getMessage();

        assertMessageContains(message, "spring.datasource.password");
        assertMessageContains(message, "SPRING_DATASOURCE_PASSWORD");

    }

    private void assertMessageContains(String message, String part) {
        if (!message.contains(part)) {
            fail("Message did not contain part: " + part + "\nMessage was: " + message);
        }
    }

}
