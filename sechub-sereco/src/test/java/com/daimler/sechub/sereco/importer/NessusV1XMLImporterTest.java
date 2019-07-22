// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.test.SerecoTestFileSupport;

public class NessusV1XMLImporterTest {

	private NessusV1XMLImporter importerToTest;

	@Before
	public void before() {
		importerToTest = new NessusV1XMLImporter();
	}

	@Test
	public void xmlReportFromNessus7canBeImported() {
		/* prepare */
		String xml = SerecoTestFileSupport.INSTANCE.loadTestFile("nessus/nessus_7.0.2.result.xml");

		ImportParameter param = ImportParameter.builder().importData(xml).importId("id1").productId("Nessus").build();

		/* execute */
		ProductImportAbility ableToImport = importerToTest.isAbleToImportForProduct(param);

		/* test */
		assertEquals("Was not able to import xml!", ProductImportAbility.ABLE_TO_IMPORT, ableToImport);
	}

}
