// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import static com.daimler.sechub.sereco.test.AssertVulnerabilities.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.daimler.sechub.sereco.test.SerecoTestFileSupport;
public class NetsparkerV1XMLImporterTest {

	private SerecoTestFileSupport support = SerecoTestFileSupport.INSTANCE;
	private ProductResultImporter importerToTest;
	@Before
	public void before() {
		importerToTest = new NetsparkerV1XMLImporter();
	}

	@Test
	public void xmlReportFromNetsparkerCanBeImported() {
		/* prepare */
		String xml = SerecoTestFileSupport.INSTANCE.loadTestFile("netsparker/netsparker_v1.0.40.109_scan_result_output_vulnerabilities.xml");

		ImportParameter param = ImportParameter.builder().importData(xml).importId("id1").productId("Netsparker").build();

		/* execute */
		ProductImportAbility ableToImport = importerToTest.isAbleToImportForProduct(param);

		/*test */
		assertEquals("Was not able to import xml!",ProductImportAbility.ABLE_TO_IMPORT, ableToImport);
	}

	@Test
	public void testfile1_contains_4_vulnerablities_which_exists_in_imported_metadata() throws Exception{
		/* prepare */
		String json = support.loadTestFile(SerecoTestFileSupport.NETSPARKER_RESULT_XML_TESTFILE1);

		/* execute */
		SerecoMetaData result = importerToTest.importResult(json);
		List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

		/* test */
		assertEquals(4,vulnerabilities.size());

	}

	@Test
	public void testfile1_contains_ApacheVersionDisclosure_and_ApacheOutOfDate_in_imported_metadata() throws Exception{
		/* prepare */
		String json = support.loadTestFile(SerecoTestFileSupport.NETSPARKER_RESULT_XML_TESTFILE1);

		/* execute */
		SerecoMetaData result = importerToTest.importResult(json);
		List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

		/* test */
		for (SerecoVulnerability v: vulnerabilities) {
		    assertEquals(ScanType.WEB_SCAN,v.getScanType());
		}
		
		/* @formatter:off */
		assertVulnerabilities(vulnerabilities).
			vulnerability().
				withSeverity(SerecoSeverity.LOW).
				withURL("https://fscan.intranet.example.org/").
				withType("ApacheVersionDisclosure").
				classifiedBy().
					wasc("45").
					cwe("205").
					capec("170").
					hipaa("164.306(a), 164.308(a)").
					and().
				withDescriptionContaining("<p>Netsparker Cloud identified a version disclosure (Apache) in the target").
				isContained().
			vulnerability().
				withSeverity(SerecoSeverity.MEDIUM).
				withURL("https://fscan.intranet.example.org/").
				withType("ApacheOutOfDate").
				classifiedBy().
					owasp("A9").
					capec("310").
					pci31("6.2").
					pci32("6.2").
					owaspProactiveControls("C1").
				and().
				isContained();
		/* @formatter:on */

	}

}
