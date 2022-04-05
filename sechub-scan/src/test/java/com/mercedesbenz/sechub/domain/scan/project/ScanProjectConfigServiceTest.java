// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.domain.scan.ScanAssertService;
import com.mercedesbenz.sechub.domain.scan.project.ScanProjectConfig.ScanProjectConfigCompositeKey;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

public class ScanProjectConfigServiceTest {

    private ScanProjectConfigService toTest;

    @Before
    public void before() {
        toTest = new ScanProjectConfigService();

        toTest.repository = mock(ScanProjectConfigRepository.class);
        toTest.scanAssertService = mock(ScanAssertService.class);
        toTest.userInputAssertion = mock(UserInputAssertion.class);

        ScanProjectConfigCompositeKey key = new ScanProjectConfigCompositeKey(ScanProjectConfigID.MOCK_CONFIGURATION, "project-id1");
        ScanProjectConfig configResult = new ScanProjectConfig(key);
        configResult.setData("configData");

        Optional<ScanProjectConfig> config = Optional.ofNullable(configResult);
        when(toTest.repository.findById(eq(key))).thenReturn(config);

    }

    @Test
    public void ensures_projectid_valid() {
        /* execute */
        toTest.get("project-id1", ScanProjectConfigID.MOCK_CONFIGURATION);

        /* test */
        verify(toTest.userInputAssertion).assertIsValidProjectId(eq("project-id1"));
    }

    @Test
    public void ensures_user_has_access_without_check_access_param() {
        /* execute */
        toTest.get("project-id1", ScanProjectConfigID.MOCK_CONFIGURATION);

        /* test */
        verify(toTest.scanAssertService).assertUserHasAccessToProject("project-id1");
    }

    @Test
    public void ensures_user_has_access_with_param_true() {
        /* execute */
        toTest.get("project-id1", ScanProjectConfigID.MOCK_CONFIGURATION, true);

        /* test */
        verify(toTest.scanAssertService).assertUserHasAccessToProject("project-id1");
    }

    @Test
    public void does_NOT_ensures_user_has_access_with_param_false() {
        /* execute */
        toTest.get("project-id1", ScanProjectConfigID.MOCK_CONFIGURATION, false);

        /* test */
        verify(toTest.scanAssertService, never()).assertUserHasAccessToProject("project-id1");
    }

    @Test
    public void fetches_data_from_repository() {
        /* execute */
        ScanProjectConfig result = toTest.get("project-id1", ScanProjectConfigID.MOCK_CONFIGURATION);

        /* test */
        assertNotNull(result);
        ;
        assertEquals("configData", result.getData());
    }

    @Test
    public void fetches_null_when_no_config_found() {
        /* execute */
        ScanProjectConfig result = toTest.get("project-id2", ScanProjectConfigID.MOCK_CONFIGURATION);

        /* test */
        assertNull(result);
        ;
    }

}
