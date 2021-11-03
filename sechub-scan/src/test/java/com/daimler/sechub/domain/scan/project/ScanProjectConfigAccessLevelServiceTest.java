// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;
import com.daimler.sechub.sharedkernel.validation.ProjectIdValidation;

class ScanProjectConfigAccessLevelServiceTest {

    private static final String PROJECT1 = "project1";
    private ScanProjectConfigAccessLevelService serviceToTest;
    private ProjectIdValidation projectIdValidation;
    private ScanProjectConfigService scanProjectConfigService;

    @BeforeEach
    void beforeEach() {

        projectIdValidation = mock(ProjectIdValidation.class);
        scanProjectConfigService = mock(ScanProjectConfigService.class);

        serviceToTest = new ScanProjectConfigAccessLevelService();
        serviceToTest.projectIdValidation = projectIdValidation;
        serviceToTest.scanProjectConfigService = scanProjectConfigService;

    }

    @Test
    void change_to_null_former_full_access_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class, () -> serviceToTest.changeProjectAccessLevel(PROJECT1, null, ProjectAccessLevel.READ_ONLY));
    }

    @Test
    void change_to_read_only_with_former_null__project_id_is_validated_and_set_to_wanted_access_level_id() {
        /* prepare */
        ScanProjectConfig config = new ScanProjectConfig(ScanProjectConfigID.PROJECT_ACCESS_LEVEL, PROJECT1);

        when(scanProjectConfigService.get(eq(PROJECT1), eq(ScanProjectConfigID.PROJECT_ACCESS_LEVEL), eq(false))).thenReturn(config);
        when(scanProjectConfigService.getOrCreate(eq(PROJECT1), eq(ScanProjectConfigID.PROJECT_ACCESS_LEVEL), eq(false), any())).thenReturn(config);

        /* execute */
        serviceToTest.changeProjectAccessLevel(PROJECT1, ProjectAccessLevel.READ_ONLY, null);

        /* test */
        verify(projectIdValidation).validate(PROJECT1);
        verify(scanProjectConfigService).set(PROJECT1, ScanProjectConfigID.PROJECT_ACCESS_LEVEL, ProjectAccessLevel.READ_ONLY.getId());
    }

    @Test
    void change_to_read_only_with_former_full_access__project_id_is_validated_and_set_to_wanted_access_level_id() {
        /* prepare */
        ScanProjectConfig config = new ScanProjectConfig(ScanProjectConfigID.PROJECT_ACCESS_LEVEL, PROJECT1);
        config.setData(ProjectAccessLevel.FULL.getId());

        when(scanProjectConfigService.get(eq(PROJECT1), eq(ScanProjectConfigID.PROJECT_ACCESS_LEVEL), eq(false))).thenReturn(config);
        when(scanProjectConfigService.getOrCreate(eq(PROJECT1), eq(ScanProjectConfigID.PROJECT_ACCESS_LEVEL), eq(false), any())).thenReturn(config);

        /* execute */
        serviceToTest.changeProjectAccessLevel(PROJECT1, ProjectAccessLevel.READ_ONLY, ProjectAccessLevel.FULL);

        /* test */
        verify(projectIdValidation).validate(PROJECT1);
        verify(scanProjectConfigService).set(PROJECT1, ScanProjectConfigID.PROJECT_ACCESS_LEVEL, ProjectAccessLevel.READ_ONLY.getId());
    }

    @Test
    void fetchProjectAccessLevel_returns_result_from_scanProjectConfigService_by_getOrCreate() {
        /* prepare */
        ScanProjectConfig config = new ScanProjectConfig(ScanProjectConfigID.PROJECT_ACCESS_LEVEL, PROJECT1);
        config.setData(null);

        when(scanProjectConfigService.getOrCreate(eq(PROJECT1), eq(ScanProjectConfigID.PROJECT_ACCESS_LEVEL), eq(false), any())).thenReturn(config);

        /* part 1: test */
        ProjectAccessLevel result = serviceToTest.fetchProjectAccessLevel(PROJECT1);

        /* test */
        assertNull(result);

        /* part 2: for each level we try out as well */
        for (ProjectAccessLevel level : ProjectAccessLevel.values()) {
            /* execute */
            config.setData(level.getId());
            result = serviceToTest.fetchProjectAccessLevel(PROJECT1);

            /* test */
            assertEquals(level, result);
        }

    }

    @Test
    void isReadAllowed__project_access_level_is_full_returns_true() {
        prepareScanConfigProject1(ProjectAccessLevel.FULL);

        /* test */
        assertTrue(serviceToTest.isReadAllowed(PROJECT1));
    }

    @Test
    void isReadAllowed__project_access_level_is_read_only_returns_true() {
        prepareScanConfigProject1(ProjectAccessLevel.READ_ONLY);

        /* test */
        assertTrue(serviceToTest.isReadAllowed(PROJECT1));
    }

    @Test
    void isReadAllowed__project_access_level_is_none_returns_false() {
        /* prepare */
        prepareScanConfigProject1(ProjectAccessLevel.NONE);

        /* test */
        assertFalse(serviceToTest.isReadAllowed(PROJECT1));
    }

    @Test
    void isWriteAllowed__project_access_level_is_full_returns_true() {
        prepareScanConfigProject1(ProjectAccessLevel.FULL);

        /* test */
        assertTrue(serviceToTest.isWriteAllowed(PROJECT1));
    }

    @Test
    void isWriteAllowed__project_access_level_is_read_only_returns_false() {
        prepareScanConfigProject1(ProjectAccessLevel.READ_ONLY);

        /* test */
        assertFalse(serviceToTest.isWriteAllowed(PROJECT1));
    }

    @Test
    void isWriteAllowed__project_access_level_is_none_returns_false() {
        prepareScanConfigProject1(ProjectAccessLevel.NONE);

        /* test */
        assertFalse(serviceToTest.isWriteAllowed(PROJECT1));
    }

    private void prepareScanConfigProject1(ProjectAccessLevel level) {
        ScanProjectConfig config = new ScanProjectConfig(ScanProjectConfigID.PROJECT_ACCESS_LEVEL, PROJECT1);
        config.setData(level.getId());

        when(scanProjectConfigService.getOrCreate(eq(PROJECT1), eq(ScanProjectConfigID.PROJECT_ACCESS_LEVEL), eq(false), any())).thenReturn(config);
    }
}
