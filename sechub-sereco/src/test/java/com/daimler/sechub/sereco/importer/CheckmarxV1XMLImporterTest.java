// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import static com.daimler.sechub.sereco.test.AssertVulnerabilities.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.SerecoClassification;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
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

		/* test */
		assertEquals("Was NOT able to import xml!", ProductImportAbility.ABLE_TO_IMPORT, ableToImport);
	}
	
	@Test
    public void emptyXMLcanNotBeImported() {
        /* prepare */
        String xml = "<?xml version='1.0'?>";

        ImportParameter param = ImportParameter.builder().importData(xml).importId("id1").productId("Checkmarx").build();

        /* execute */
        ProductImportAbility ableToImport = importerToTest.isAbleToImportForProduct(param);

        /* test */
        assertEquals("Was able to import xml!", ProductImportAbility.NOT_ABLE_TO_IMPORT, ableToImport);
    }
	
	@Test
    public void bookStoreExampleXMLcanNotBeImported() {
        /* prepare */
        String xml = "<?xml version='1.0'?><bookstore><available><book name='lord of the rings' id='!'/></available></bookstore>";

        ImportParameter param = ImportParameter.builder().importData(xml).importId("id1").productId("Checkmarx").build();

        /* execute */
        ProductImportAbility ableToImport = importerToTest.isAbleToImportForProduct(param);

        /* test */
        assertEquals("Was able to import xml!", ProductImportAbility.NOT_ABLE_TO_IMPORT, ableToImport);
    }

	@Test
	public void xmlReportFromCheckmarxVhasNoDescriptionButCodeInfo() throws Exception {
		/* prepare */
		String xml = SerecoTestFileSupport.INSTANCE.loadTestFile("checkmarx/sechub-continous-integration-with-false-positive.xml");

		/* execute */
		SerecoMetaData result = importerToTest.importResult(xml);

		/* test */
		List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

		SerecoVulnerability v1 = fetchFirstNonFalsePositive(vulnerabilities);
		assertEquals(SerecoSeverity.MEDIUM, v1.getSeverity());
		assertEquals("", v1.getDescription());
		assertEquals(ScanType.CODE_SCAN,v1.getScanType());

		SerecoCodeCallStackElement codeInfo = v1.getCode();
		assertNotNull(codeInfo);
		/*
		 * v1 is not first entry, because first entry was a false positive which was
		 * already filtered
		 */
		assertEquals("com/daimler/sechub/server/IntegrationTestServerRestController.java", codeInfo.getLocation());
		assertEquals(Integer.valueOf(86), codeInfo.getLine());
		assertEquals(Integer.valueOf(37), codeInfo.getColumn());
		assertEquals("			@PathVariable(\"fileName\") String fileName) throws IOException {",codeInfo.getSource());
		assertEquals("fileName",codeInfo.getRelevantPart());

		SerecoCodeCallStackElement calls1 = codeInfo.getCalls();
		assertNotNull(calls1);
		SerecoCodeCallStackElement calls2 = calls1.getCalls();
		assertNotNull(calls2);

		assertEquals("com/daimler/sechub/sharedkernel/storage/JobStorage.java", calls2.getLocation());
		assertEquals(Integer.valueOf(139), calls2.getLine());
		assertEquals(Integer.valueOf(39), calls2.getColumn());
		assertEquals("	public String getAbsolutePath(String fileName) {",calls2.getSource());
		assertEquals("fileName",codeInfo.getRelevantPart());

	}

	@Test
	public void xmlReportFromCheckmarxV8containsDeeplink() throws Exception {
		/* prepare */
		String xml = SerecoTestFileSupport.INSTANCE.loadTestFile("checkmarx/sechub-continous-integration-with-false-positive.xml");

		/* execute */
		SerecoMetaData result = importerToTest.importResult(xml);

		/* test */
		List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

		SerecoVulnerability v1 = fetchFirstNonFalsePositive(vulnerabilities);
		assertEquals(SerecoSeverity.MEDIUM, v1.getSeverity());

		assertEquals("https://defvm1676.intranet.example.org/CxWebClient/ViewerMain.aspx?scanid=1000866&projectid=279&pathid=2", v1.getProductResultLink());

	}

	@Test
	public void xmlReportFromCheckmarxV8_with_false_positve_canBeImported_and_contains_not_false_positive() throws IOException {
		/* prepare */
		String xml = SerecoTestFileSupport.INSTANCE.loadTestFile("checkmarx/sechub-continous-integration-with-false-positive.xml");

		/* execute */
		SerecoMetaData data = importerToTest.importResult(xml);

		/* test @formatter:off */
		assertVulnerabilities(data.getVulnerabilities()).
			vulnerability().withSeverity(SerecoSeverity.HIGH).isNotContained(true). /* ONE is  high but false positive*/
			hasVulnerabilities(240).
			hasVulnerabilities(230,true); /* inside xml there are 240 vulnerabilities, but 10 are false positives */
		/* @formatter:on */
	}

	@Test
	public void load_example1_contains_expected_data() throws IOException {
		/* prepare */
		String xml = SerecoTestFileSupport.INSTANCE.loadTestFileFromRoot("sechub-other/testoutput/checkmarx-example1.xml");

		/* execute */
		SerecoMetaData data = importerToTest.importResult(xml);

		/* test */
		List<SerecoVulnerability> vulnerabilities = data.getVulnerabilities();

		assertEquals(109, vulnerabilities.size());

		SerecoVulnerability v1 = vulnerabilities.get(0);
		assertEquals(SerecoSeverity.MEDIUM, v1.getSeverity());
		SerecoClassification classification = v1.getClassification();
		assertEquals("A5", classification.getOwasp());
		assertEquals("6.5.8", classification.getPci32());
		assertEquals(null, classification.getPci31());

		SerecoVulnerability v100 = vulnerabilities.get(99);
		assertEquals(SerecoSeverity.LOW, v100.getSeverity());
		classification = v100.getClassification();
		assertEquals(null, classification.getPci32());
		assertEquals(null, classification.getOwasp());
		assertEquals("AC-3", classification.getNist());
		assertEquals("Identification And Authentication", classification.getFisma());

		SerecoVulnerability v109 = vulnerabilities.get(108);
		assertEquals(SerecoSeverity.INFO, v109.getSeverity());
		classification = v109.getClassification();
		assertEquals(null, classification.getOwasp());
		assertEquals(null, classification.getPci32());
		assertEquals(null, classification.getNist());
		assertEquals(null, classification.getFisma());
		assertEquals("778", classification.getCwe());

		assertEquals("Insufficient Logging of Exceptions", v109.getType());
	}

	 private SerecoVulnerability fetchFirstNonFalsePositive(List<SerecoVulnerability> vulnerabilities) {
	        Iterator<SerecoVulnerability> vit = vulnerabilities.iterator();
	        SerecoVulnerability v1 = vit.next();
	        while (v1.isFalsePositive()) {
	            /* we have also false positives here .. and just search for first non false Positive*/
	            v1 = vit.next();
	        }
	        return v1;
	    }
}
