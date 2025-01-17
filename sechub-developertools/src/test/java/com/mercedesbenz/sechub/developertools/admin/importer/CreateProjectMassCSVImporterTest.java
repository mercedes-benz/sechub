// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.importer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mercedesbenz.sechub.developertools.TestDeveloperToolsFileSupport;
import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration;
import com.mercedesbenz.sechub.developertools.admin.ui.OutputUI;
import com.mercedesbenz.sechub.developertools.admin.ui.UIContext;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class CreateProjectMassCSVImporterTest {

    private CreateProjectMassCSVImporter importerToTest;

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private DeveloperAdministration administration;

    @Before
    public void before() {
        administration = mock(DeveloperAdministration.class);
        importerToTest = new CreateProjectMassCSVImporter(administration);
        UIContext uiContext = mock(UIContext.class);
        OutputUI outputUI = mock(OutputUI.class);
        when(uiContext.getOutputUI()).thenReturn(outputUI);
        when(administration.getUiContext()).thenReturn(uiContext);
    }

    @Test
    public void example_1_projects_can_be_imported() throws Exception {
        /* prepare */
        File file = TestDeveloperToolsFileSupport.getTestfileSupport().createFileFromResourcePath("csv/example1-developer-admin-ui_mass-import_projects.csv");

        /* execute */
        importerToTest.importProjectsAndRelationsByCSV(file);

        /* test */
        for (int i = 1; i <= 15; i++) {
            verify(administration, times(1)).createProject(eq("testproject_" + i), eq("Project testproject_" + i), eq("scenario2_owner1"),
                    eq(Collections.emptyList()), eq(Collections.emptyMap()));
        }
        verify(administration).assignUserToProject("scenario2_user1", "testproject_1");

        verify(administration).assignUserToProject("scenario2_user2", "testproject_2");
        verify(administration, never()).assignUserToProject("scenario2_user1", "testproject_2");

        verify(administration).assignUserToProject("scenario2_user1", "testproject_3");
        verify(administration).assignUserToProject("scenario2_user2", "testproject_3");

    }

    @Test
    public void example_6_projects_can_be_imported_and_releations_to_profiles_are_added() throws Exception {
        /* prepare */
        File file = TestDeveloperToolsFileSupport.getTestfileSupport()
                .createFileFromResourcePath("csv/example6-developer-admin-ui_mass-import_projects-with-profiles.csv");

        /* execute */
        importerToTest.importProjectsAndRelationsByCSV(file);

        /* test */
        for (int i = 1; i <= 15; i++) {
            verify(administration, times(1)).createProject(eq("testproject_" + i), eq("Project testproject_" + i), eq("scenario2_owner1"),
                    eq(Collections.emptyList()), eq(Collections.emptyMap()));
        }
        verify(administration).assignUserToProject("scenario2_user1", "testproject_1");

        verify(administration).assignUserToProject("scenario2_user2", "testproject_2");
        verify(administration, never()).assignUserToProject("scenario2_user1", "testproject_2");

        verify(administration).assignUserToProject("scenario2_user1", "testproject_3");
        verify(administration).assignUserToProject("scenario2_user2", "testproject_3");

        // profile check
        verify(administration).addProjectIdsToProfile("profilea", "testproject_1");
        verify(administration).addProjectIdsToProfile("profileb", "testproject_1");
        verify(administration).addProjectIdsToProfile("profilec", "testproject_2");
        verify(administration).addProjectIdsToProfile("profiled", "testproject_8");
        verify(administration).addProjectIdsToProfile("profilee", "testproject_8");

    }
}
