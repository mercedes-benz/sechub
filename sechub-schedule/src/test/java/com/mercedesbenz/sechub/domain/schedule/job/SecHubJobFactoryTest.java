// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.ModuleGroup;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.sharedkernel.UserContextService;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

class SecHubJobFactoryTest {

    private SecHubJobFactory factoryToTest;
    private UserContextService userContextService;
    private SecHubConfigurationModelSupport modelSupport;

    @BeforeEach
    void beforeEach() {
        userContextService = mock(UserContextService.class);
        modelSupport = mock(SecHubConfigurationModelSupport.class);

        factoryToTest = new SecHubJobFactory();
        factoryToTest.userContextService = userContextService;
        factoryToTest.modelSupport = modelSupport;
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { "user1", "x-y-z" })
    void createJob_uses_user_id_from_usercontext_service_used(String userName) {
        /* prepare */
        when(userContextService.getUserId()).thenReturn(userName);
        SecHubConfiguration configuration = mock(SecHubConfiguration.class);

        /* execute */
        ScheduleSecHubJob result = factoryToTest.createJob(configuration);

        /* test */
        assertNotNull(result);
        assertEquals(userName, result.getOwner());

    }

    @Test
    void createJob_fails_when_usercontext_returns_user_uuid_with_null() {
        /* prepare */
        when(userContextService.getUserId()).thenReturn(null);
        SecHubConfiguration configuration = mock(SecHubConfiguration.class);

        /* execute */
        assertThrows(IllegalStateException.class, () -> factoryToTest.createJob(configuration));
    }

    @Test
    void createJob_sets_creation_time() {
        /* prepare */
        when(userContextService.getUserId()).thenReturn("user1");
        SecHubConfiguration configuration = mock(SecHubConfiguration.class);

        /* execute */
        ScheduleSecHubJob result = factoryToTest.createJob(configuration);

        /* test */
        assertNotNull(result);
        assertNotNull(result.getCreated());

    }

    @Test
    void createJob_sets_configuration_as_json() {
        /* prepare */
        when(userContextService.getUserId()).thenReturn("user1");
        SecHubConfiguration configuration = mock(SecHubConfiguration.class);
        when(configuration.toJSON()).thenReturn("pseudo-json");

        /* execute */
        ScheduleSecHubJob result = factoryToTest.createJob(configuration);

        /* test */
        assertNotNull(result);
        assertEquals("pseudo-json", result.getJsonConfiguration());
    }

    @Test
    void createJob_sets_project_id_from_configuration() {
        /* prepare */
        when(userContextService.getUserId()).thenReturn("user1");
        SecHubConfiguration configuration = mock(SecHubConfiguration.class);
        when(configuration.getProjectId()).thenReturn("project-id");

        /* execute */
        ScheduleSecHubJob result = factoryToTest.createJob(configuration);

        /* test */
        assertNotNull(result);
        assertEquals("project-id", result.getProjectId());
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class)
    void createJob_calculates_scan_group_from_data_given_by_model_suppport(ScanType type) {
        /* prepare */
        when(userContextService.getUserId()).thenReturn("user1");
        SecHubConfiguration configuration = mock(SecHubConfiguration.class);
        when(modelSupport.collectPublicScanTypes(configuration)).thenReturn(Collections.singleton(type));

        /* execute */
        ScheduleSecHubJob result = factoryToTest.createJob(configuration);

        /* test */
        assertNotNull(result);

        ModuleGroup expected = null;
        if (!type.isInternalScanType()) {
            /* for public types the expected group is not null */
            switch (type) {
            case INFRA_SCAN:
                expected = ModuleGroup.NETWORK;
                break;
            case WEB_SCAN:
                expected = ModuleGroup.DYNAMIC;
                break;
            default:
                // we assume currently all others are "content" (e.g. license scan, code scan
                // etc.)
                expected = ModuleGroup.STATIC;
                break;

            }
        }
        assertEquals(expected, result.getModuleGroup());
    }

}
