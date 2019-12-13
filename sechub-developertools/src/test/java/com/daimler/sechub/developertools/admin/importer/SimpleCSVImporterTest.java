package com.daimler.sechub.developertools.admin.importer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.developertools.DeveloperToolsTestFileSupport;

public class SimpleCSVImporterTest {

	private SimpleCSVImporter importerToTest;

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Before
	public void before() {
		importerToTest = new SimpleCSVImporter();
	}

	@Test
	public void example1_can_be_read_with_3_columns_and_1_header_and_contains_expected_data() throws Exception {
		/* prepare */
		File file = DeveloperToolsTestFileSupport.getTestfileSupport().createFileFromResourcePath("csv/example1-developer-admin-ui_mass-import_projects.csv");

		/* execute */
		List<ImportCSVRow> importedRows = importerToTest.importCSVFile(file, 3, 1);

		/* test */
		assertEquals(15, importedRows.size()); // 16 lines- 1 header, so line 2 inside CSV is here 0
		assertEquals("testproject_1", importedRows.get(0).columns.get(0).cell);

		assertEquals("scenario2_owner1", importedRows.get(11).columns.get(1).cell);
		assertEquals("scenario2_user1,scenario2_user2", importedRows.get(11).columns.get(2).cell);

		assertEquals("testproject_15", importedRows.get(14).columns.get(0).cell);
		assertEquals("scenario2_owner1", importedRows.get(14).columns.get(1).cell);
		assertEquals("scenario2_user4", importedRows.get(14).columns.get(2).cell);

	}

	@Test
	public void example1_can_not_be_read_with_2_columns() throws Exception {
		/* test*/
		expected.expect(IllegalStateException.class);

		/* prepare */
		File file = DeveloperToolsTestFileSupport.getTestfileSupport().createFileFromResourcePath("csv/example1-developer-admin-ui_mass-import_projects.csv");

		/* execute */
		importerToTest.importCSVFile(file, 2, 1);
	}

	@Test
	public void example1_can_not_be_read_with_4_columns() throws Exception {
		/* test*/
		expected.expect(IllegalStateException.class);

		/* prepare */
		File file = DeveloperToolsTestFileSupport.getTestfileSupport().createFileFromResourcePath("csv/example1-developer-admin-ui_mass-import_projects.csv");

		/* execute */
		importerToTest.importCSVFile(file, 2, 1);
	}

}
