// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.administration.project;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.administration.user.User;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.error.NotFoundException;
import com.daimler.sechub.sharedkernel.logging.LogSanitizer;
import com.daimler.sechub.sharedkernel.validation.UserInputAssertion;

public class ProjectDetailChangeServiceTest {

    private static String PROJECT_ID = "project1";

    private ProjectDetailChangeService serviceToTest;

    private ProjectRepository projectRepository;
    private UserInputAssertion assertion;
    private ProjectTransactionService transactionService;

    @Before
    public void before() throws Exception {
        projectRepository = mock(ProjectRepository.class);
        assertion = mock(UserInputAssertion.class);
        transactionService = mock(ProjectTransactionService.class);

        serviceToTest = new ProjectDetailChangeService();
        serviceToTest.assertion = assertion;
        serviceToTest.projectRepository = projectRepository;
        serviceToTest.transactionService = transactionService;
        serviceToTest.logSanitizer = mock(LogSanitizer.class);
    }

    @Test
    public void when_change_description_called_changed_project_will_be_stored() {
        /* prepare */
        Project project = new Project();
        project.id = PROJECT_ID;
        project.description = "old";
        project.owner = new User();

        String json = "{\n" + "    \"description\": \"new\"\n" + "}";

        ProjectJsonInput withNewDescription = new ProjectJsonInput();
        withNewDescription = withNewDescription.fromJSON(json);

        when(projectRepository.findOrFailProject(PROJECT_ID)).thenReturn(project);
        when(transactionService.saveInOwnTransaction(project)).thenReturn(project);

        /* execute */
        serviceToTest.changeProjectDescription(PROJECT_ID, withNewDescription);

        /* test */
        verify(transactionService).saveInOwnTransaction(project);
    }

    @Test
    public void change_something_else_than_description() {

        /* prepare */
        Project project = new Project();
        project.id = PROJECT_ID;
        project.description = "old";
        project.owner = new User();

        String json = "{\"owner\": \"newOwner\"}";

        when(projectRepository.findOrFailProject(PROJECT_ID)).thenReturn(project);

        /* execute + test */
        assertThrows(NotAcceptableException.class, () -> {

            // to avoid `should be final`error this object is created within this scope
            ProjectJsonInput withNewOwner = new ProjectJsonInput();
            withNewOwner = withNewOwner.fromJSON(json);

            serviceToTest.changeProjectDescription("project2", withNewOwner);
        });

    }

    @Test
    public void change_description_but_project_does_not_exist() {

        /* prepare */
        String json = "{\"description\": \"new\"}";

        when(projectRepository.findOrFailProject("project2")).thenThrow(new NotFoundException());

        /* execute + test */
        assertThrows(NotFoundException.class, () -> {

            ProjectJsonInput withNewDescription = new ProjectJsonInput();
            withNewDescription = withNewDescription.fromJSON(json);

            serviceToTest.changeProjectDescription("project2", withNewDescription);
        });

    }

}
