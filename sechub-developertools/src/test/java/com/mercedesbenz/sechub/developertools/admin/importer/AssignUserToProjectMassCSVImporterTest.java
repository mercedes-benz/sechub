// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.importer;

import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mercedesbenz.sechub.developertools.TestDeveloperToolsFileSupport;
import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class AssignUserToProjectMassCSVImporterTest {

    private AssignUserToProjectMassCSVImporter importerToTest;

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private DeveloperAdministration administration;

    @Before
    public void before() {
        administration = mock(DeveloperAdministration.class);
        importerToTest = new AssignUserToProjectMassCSVImporter(administration);
    }

    @Test
    public void example_3_user2projects_can_be_imported() throws Exception {
        /* prepare */
        File file = TestDeveloperToolsFileSupport.getTestfileSupport()
                .createFileFromResourcePath("csv/example3-developer-admin-ui_mass-import_user2projects.csv");

        /* execute */
        importerToTest.importUsersToProjectAssignmentsByCSV(file);

        /* test */
        verify(administration).assignUserToProject("scenario2_user1", "testproject_1");

        verify(administration).assignUserToProject("scenario2_user2", "testproject_2");
        verify(administration, never()).assignUserToProject("scenario2_user1", "testproject_2");

        verify(administration).assignUserToProject("scenario2_user1", "testproject_3");
        verify(administration).assignUserToProject("scenario2_user2", "testproject_3");

    }
}
