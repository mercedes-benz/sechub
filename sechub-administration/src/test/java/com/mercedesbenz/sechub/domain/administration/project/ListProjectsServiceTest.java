// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ListProjectsServiceTest {

    private ListProjectsService serviceToTest;
    private ProjectRepository projectRepository;

    @Before
    public void before() throws Exception {
        projectRepository = mock(ProjectRepository.class);

        serviceToTest = new ListProjectsService();
        serviceToTest.projectRepository = projectRepository;
    }

    @Test
    public void list_projects_uses_findAllProjectIds_from_repo() {
        /* prepare */
        List<String> expectedProjectIds = new ArrayList<>();
        expectedProjectIds.add("Project1");
        expectedProjectIds.add("Project2");

        when(projectRepository.findAllProjectIdsOrdered()).thenReturn(expectedProjectIds);

        /* execute */
        List<String> actualProjectIds = serviceToTest.listProjects();

        /* test */
        assertEquals(expectedProjectIds, actualProjectIds);
    }
}
