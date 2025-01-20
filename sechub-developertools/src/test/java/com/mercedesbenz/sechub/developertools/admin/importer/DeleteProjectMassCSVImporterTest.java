// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.admin.importer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mercedesbenz.sechub.developertools.TestDeveloperToolsFileSupport;
import com.mercedesbenz.sechub.developertools.admin.DeveloperAdministration;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class DeleteProjectMassCSVImporterTest {

    private DeleteProjectMassCSVImporter importerToTest;

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();

    private DeveloperAdministration administration;

    @Before
    public void before() {
        administration = mock(DeveloperAdministration.class);
        importerToTest = new DeleteProjectMassCSVImporter(administration);
    }

    @Test
    public void example_1_projects_can_be_imported() throws Exception {
        /* prepare */
        File file = TestDeveloperToolsFileSupport.getTestfileSupport()
                .createFileFromResourcePath("csv/example2-developer-admin-ui_mass-import_delete_projects.csv");

        /* execute */
        importerToTest.importProjectDeletesByCSV(file);

        /* test */
        for (int i = 1; i <= 15; i++) {
            verify(administration, times(1)).deleteProject(eq("testproject_" + i));
        }

    }
}
