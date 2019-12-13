package com.daimler.sechub.developertools.admin.importer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.developertools.DeveloperToolsTestFileSupport;
import com.daimler.sechub.developertools.admin.DeveloperAdministration;

public class CreateProjectMassCSVImporterTest {

	private CreateProjectMassCSVImporter importerToTest;

	@Rule
	public ExpectedException expected = ExpectedException.none();

	private DeveloperAdministration administration;

	@Before
	public void before() {
		administration = mock(DeveloperAdministration.class);
		importerToTest = new CreateProjectMassCSVImporter(administration);
	}

	@Test
	public void example_1_projects_can_be_imported() throws Exception {
		/* prepare */
		File file = DeveloperToolsTestFileSupport.getTestfileSupport().createFileFromResourcePath("csv/example1-developer-admin-ui_mass-import_projects.csv");

		/* execute */
		importerToTest.importProjectsAndRelationsByCSV(file);

		/* test */
		for (int i=1;i<=15;i++) {
			verify(administration,times(1)).createProject(eq("testproject_"+i),eq("Project testproject_"+i),eq("scenario2_owner1"),eq(Collections.emptyList()));
		}
		verify(administration).assignUserToProject("scenario2_user1", "testproject_1");

		verify(administration).assignUserToProject("scenario2_user2", "testproject_2");
		verify(administration,never()).assignUserToProject("scenario2_user1", "testproject_2");

		verify(administration).assignUserToProject("scenario2_user1", "testproject_3");
		verify(administration).assignUserToProject("scenario2_user2", "testproject_3");

	}
}
