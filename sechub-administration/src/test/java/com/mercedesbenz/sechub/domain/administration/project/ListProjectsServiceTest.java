// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
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
    public void list_projects() {
        /* prepare */
        List<String> expectedProjectIds = new ArrayList<>();
        expectedProjectIds.add("Project1");
        expectedProjectIds.add("Project2");

        Project project1 = mock(Project.class);
        Project project2 = mock(Project.class);

        when(project1.getId()).thenReturn(expectedProjectIds.get(0));
        when(project2.getId()).thenReturn(expectedProjectIds.get(1));

        List<Project> projects = new LinkedList<>();
        projects.add(project1);
        projects.add(project2);

        when(projectRepository.findAll()).thenReturn(projects);

        /* execute */
        List<String> actualProjectIds = serviceToTest.listProjects();

        /* test */
        assertEquals(expectedProjectIds, actualProjectIds);
    }
}
