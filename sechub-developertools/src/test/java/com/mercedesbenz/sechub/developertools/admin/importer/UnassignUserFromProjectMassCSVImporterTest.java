// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.importer;

import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mercedesbenz.sechub.developertools.DeveloperToolsTestFileSupport;
import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class UnassignUserFromProjectMassCSVImporterTest {

    private UnassignUserToProjectMassCSVImporter importerToTest;

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private DeveloperAdministration administration;

    @Before
    public void before() {
        administration = mock(DeveloperAdministration.class);
        importerToTest = new UnassignUserToProjectMassCSVImporter(administration);
    }

    @Test
    public void example_3_user2projects_can_be_imported() throws Exception {
        /* prepare */
        File file = DeveloperToolsTestFileSupport.getTestfileSupport()
                .createFileFromResourcePath("csv/example4-developer-admin-ui_mass-import_user2projects-unassign.csv");

        /* execute */
        importerToTest.importUsersFromProjectUnassignmentsByCSV(file);

        /* test */
        verify(administration).unassignUserFromProject("scenario2_user1", "testproject_1");

        verify(administration).unassignUserFromProject("scenario2_user2", "testproject_2");
        verify(administration, never()).unassignUserFromProject("scenario2_user1", "testproject_2");

        verify(administration).unassignUserFromProject("scenario2_user1", "testproject_3");
        verify(administration).unassignUserFromProject("scenario2_user2", "testproject_3");

    }
}
