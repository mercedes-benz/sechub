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
    public void change_description() {
        Project project = new Project();
        project.id = PROJECT_ID;
        project.description = "old";
        project.owner = new User();

        String json = "{\n" + "    \"description\": \"new\"\n" + "}";

        ProjectJsonInput withNewDescription = new ProjectJsonInput();
        withNewDescription = withNewDescription.fromJSON(json);

        when(projectRepository.findOrFailProject(PROJECT_ID)).thenReturn(project);
        when(transactionService.saveInOwnTransaction(project)).thenReturn(project);

        serviceToTest.changeDetails(PROJECT_ID, withNewDescription);

        verify(transactionService).saveInOwnTransaction(project);

    }

    @Test
    public void change_something_else_than_description() {

        Project project = new Project();
        project.id = PROJECT_ID;
        project.description = "old";
        project.owner = new User();

        String json = "{\"owner\": \"newOwner\"}";

        when(projectRepository.findOrFailProject(PROJECT_ID)).thenReturn(project);

        assertThrows(NotAcceptableException.class, () -> {

            ProjectJsonInput withNewOwner = new ProjectJsonInput();
            withNewOwner = withNewOwner.fromJSON(json);

            serviceToTest.changeDetails("project2", withNewOwner);
        });

    }

    @Test
    public void change_description_but_project_does_not_exist() {

        String json = "{\"description\": \"new\"}";

        when(projectRepository.findOrFailProject("project2")).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class, () -> {

            ProjectJsonInput withNewDescription = new ProjectJsonInput();
            withNewDescription = withNewDescription.fromJSON(json);

            serviceToTest.changeDetails("project2", withNewDescription);
        });

    }

}
