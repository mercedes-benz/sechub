// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.domain.schedule.ScheduleAssertService;
import com.daimler.sechub.sharedkernel.project.ProjectAccessLevel;

class SchedulerProjectConfigServiceTest {
    
    private static final String PROJECT1 = "project1";
    private SchedulerProjectConfigService serviceToTest;
    private SchedulerProjectConfigRepository repository;
    private ScheduleAssertService assertService;

    @BeforeEach
    void beforeEach() {
        repository=mock(SchedulerProjectConfigRepository.class);
        assertService=mock(ScheduleAssertService.class);
        
        serviceToTest = new SchedulerProjectConfigService();
        serviceToTest.assertService=assertService;
        serviceToTest.repository=repository;
        
    }
    

    @Test
    void change_to_null_former_full_access_throws_illegal_argument() {
        assertThrows(IllegalArgumentException.class, () -> serviceToTest.changeProjectAccessLevel(PROJECT1, null, ProjectAccessLevel.READ_ONLY));
    }

    @Test
    void change_to_read_only_with_former_null__project_id_is_validated_and_set_to_wanted_access_level_id() {
        /* prepare */
        SchedulerProjectConfig config = new SchedulerProjectConfig();
        config.projectId=PROJECT1;
        config.projectAccessLevel=ProjectAccessLevel.FULL;

        when(repository.findById(PROJECT1)).thenReturn(Optional.of(config));

        /* execute */
        serviceToTest.changeProjectAccessLevel(PROJECT1, ProjectAccessLevel.READ_ONLY, null);

        /* test */
        verify(repository).save(config);
        verify(assertService).asserProjectIdValid(PROJECT1);
        assertEquals(ProjectAccessLevel.READ_ONLY,config.getProjectAccessLevel());
    }

    @Test
    void change_to_read_only_with_former_full_access__project_id_is_validated_and_set_to_wanted_access_level_id() {
        /* prepare */
        SchedulerProjectConfig config = new SchedulerProjectConfig();
        config.projectId=PROJECT1;
        config.projectAccessLevel=ProjectAccessLevel.FULL;

        when(repository.findById(PROJECT1)).thenReturn(Optional.of(config));

        /* execute */
        serviceToTest.changeProjectAccessLevel(PROJECT1, ProjectAccessLevel.READ_ONLY, null);

        /* test */
        verify(repository).save(config);
        verify(assertService).asserProjectIdValid(PROJECT1);
        assertEquals(ProjectAccessLevel.READ_ONLY,config.getProjectAccessLevel());
    }

    @Test
    void fetchProjectAccessLevel_returns_result_from_scanprojectConfigService_by_getOrCreate() {
        /* prepare */
        SchedulerProjectConfig config = new SchedulerProjectConfig();
        config.projectId=PROJECT1;
        config.projectAccessLevel=null;

        when(repository.findById(PROJECT1)).thenReturn(Optional.of(config));

        /* part 1: test */
        ProjectAccessLevel result = serviceToTest.getProjectAccessLevel(PROJECT1);

        /* test */
        assertNull(result);

        /* part 2: for each level we try out as well */
        for (ProjectAccessLevel level : ProjectAccessLevel.values()) {
            /* execute */
            config.projectAccessLevel=level;
            result = serviceToTest.getProjectAccessLevel(PROJECT1);

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
        SchedulerProjectConfig config = new SchedulerProjectConfig();
        config.projectId=PROJECT1;
        config.projectAccessLevel=level;

        when(repository.findById(PROJECT1)).thenReturn(Optional.of(config));
    }
}
