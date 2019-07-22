// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import static com.daimler.sechub.sereco.test.AssertVulnerabilities.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.Classification;
import com.daimler.sechub.sereco.metadata.MetaData;
import com.daimler.sechub.sereco.metadata.Severity;
import com.daimler.sechub.sereco.metadata.Vulnerability;
import com.daimler.sechub.sereco.test.SerecoTestFileSupport;
public class CheckmarxV1XMLImporterTest {

	private CheckmarxV1XMLImporter importerToTest;

	@Before
	public void before() {
		importerToTest = new CheckmarxV1XMLImporter();
	}

	@Test
	public void xmlReportFromCheckmarxV8canBeImported() {
		/* prepare */
		String xml = SerecoTestFileSupport.INSTANCE.loadTestFile("checkmarx/sechub-continous-integration.xml");

		ImportParameter param = ImportParameter.builder().importData(xml).importId("id1").productId("Checkmarx").build();

		/* execute */
		ProductImportAbility ableToImport = importerToTest.isAbleToImportForProduct(param);

		/*test */
		assertEquals("Was not able to import xml!",ProductImportAbility.ABLE_TO_IMPORT,ableToImport);
	}

	@Test
	public void xmlReportFromCheckmarxV8_with_false_positve_canBeImported_and_contains_not_false_positive() throws IOException {
		/* prepare */
		String xml = SerecoTestFileSupport.INSTANCE.loadTestFile("checkmarx/sechub-continous-integration-with-false-positive.xml");

		/* execute */
		MetaData data = importerToTest.importResult(xml);

		/* test @formatter:off */
		assertVulnerabilities(data.getVulnerabilities()).
			vulnerability().withSeverity(Severity.HIGH).isNotContained(). /* ONE is  high but false positive*/
			hasVulnerabilities(230); /* inside xml there are 240 vulnerabilities, but 10 are false positives */
		/* @formatter:on */
	}

	@Test
	public void load_example1_contains_expected_data() throws IOException {
		/* prepare */
		String xml = SerecoTestFileSupport.INSTANCE.loadTestFileFromRoot("sechub-other/testoutput/checkmarx-example1.xml");

		/* execute */
		MetaData data = importerToTest.importResult(xml);

		/* test */
		List<Vulnerability> vulnerabilities = data.getVulnerabilities();

		assertEquals(109, vulnerabilities.size());

		Vulnerability v1 = vulnerabilities.get(0);
		assertEquals(Severity.MEDIUM,v1.getSeverity());
		Classification classification = v1.getClassification();
		assertEquals("A5",classification.getOwasp());
		assertEquals("6.5.8",classification.getPci32());
		assertEquals("",classification.getPci31());

		Vulnerability v100 = vulnerabilities.get(99);
		assertEquals(Severity.LOW,v100.getSeverity());
		classification = v100.getClassification();
		assertEquals("",classification.getPci32());
		assertEquals("",classification.getOwasp());
		assertEquals("AC-3",classification.getNist());
		assertEquals("Identification And Authentication",classification.getFisma());

		Vulnerability v109 = vulnerabilities.get(108);
		assertEquals(Severity.INFO,v109.getSeverity());
		classification = v109.getClassification();
		assertEquals("",classification.getOwasp());
		assertEquals("",classification.getPci32());
		assertEquals("",classification.getNist());
		assertEquals("",classification.getFisma());
		assertEquals("778",classification.getCwe());

		assertEquals("Insufficient Logging of Exceptions", v109.getType());
	}

}
