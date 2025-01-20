// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static com.mercedesbenz.sechub.sereco.test.AssertVulnerabilities.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.test.TestSerecoFileSupport;

public class NetsparkerV1XMLImporterTest {

    private TestSerecoFileSupport support = TestSerecoFileSupport.INSTANCE;
    private ProductResultImporter importerToTest;

    @BeforeEach
    void before() {
        importerToTest = new NetsparkerV1XMLImporter();
    }

    @Test
    void xmlReportFromNetsparkerCanBeImported() {
        /* prepare */
        String xml = TestSerecoFileSupport.INSTANCE.loadTestFile("netsparker/netsparker_v1.0.40.109_scan_result_output_vulnerabilities.xml");

        ImportParameter param = ImportParameter.builder().importData(xml).importId("id1").productId("Netsparker").build();

        /* execute */
        boolean ableToImport = importerToTest.isAbleToImportForProduct(param);

        /* test */
        assertTrue(ableToImport, "Was not able to import xml!");
    }

    @Test
    void testfile1_contains_4_vulnerablities_which_exists_in_imported_metadata() throws Exception {
        /* prepare */
        String xml = support.loadTestFile(TestSerecoFileSupport.NETSPARKER_RESULT_XML_TESTFILE1);

        /* execute */
        SerecoMetaData result = importerToTest.importResult(xml, ScanType.WEB_SCAN);
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        assertEquals(4, vulnerabilities.size());

    }

    @Test
    void testfile1_contains_ApacheVersionDisclosure_and_ApacheOutOfDate_in_imported_metadata() throws Exception {
        /* prepare */
        String xml = support.loadTestFile(TestSerecoFileSupport.NETSPARKER_RESULT_XML_TESTFILE1);

        /* execute */
        SerecoMetaData result = importerToTest.importResult(xml, ScanType.WEB_SCAN);
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        for (SerecoVulnerability vulnerability : vulnerabilities) {
            assertEquals(ScanType.WEB_SCAN, vulnerability.getScanType());
        }

        /* @formatter:off */
		assertVulnerabilities(vulnerabilities).
			vulnerability().
				withSeverity(SerecoSeverity.LOW).
				isExactDefinedWebVulnerability().
				    withTarget("https://fscan.intranet.example.org/").
				and().
				withType("ApacheVersionDisclosure").
				classifiedBy().
					wasc("45").
					cwe("205").
					capec("170").
					hipaa("164.306(a), 164.308(a)").
					and().
				withDescriptionContaining("Netsparker Cloud identified a version disclosure (Apache) in the target").
				isContained().
			vulnerability().
				withSeverity(SerecoSeverity.MEDIUM).
				isExactDefinedWebVulnerability().
                    withTarget("https://fscan.intranet.example.org/").
                and().
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

    @Test
    void test_xml_import_netsparker_1_9_1_977_can_be_imported() throws Exception {
        /* prepare */
        String xml = TestSerecoFileSupport.INSTANCE.loadTestFile(TestSerecoFileSupport.NETSPARKER_V1_9_1_977_XML_TESTFILE);

        ImportParameter param = ImportParameter.builder().importData(xml).importId("id1").productId("Netsparker").build();

        /* execute */
        boolean ableToImport = importerToTest.isAbleToImportForProduct(param);

        /* test */
        assertTrue(ableToImport, "Was not able to import xml!");
    }

    @Test
    void test_xml_import_netsparker_1_9_1_977_contains_4_vulnerablities() throws Exception {
        /* prepare */
        String xml = support.loadTestFile(TestSerecoFileSupport.NETSPARKER_V1_9_1_977_XML_TESTFILE);

        /* execute */
        SerecoMetaData result = importerToTest.importResult(xml, ScanType.WEB_SCAN);
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        assertEquals(4, vulnerabilities.size());
    }

    @Test
    void test_xml_import_netsparker_1_9_1_977_contains_specific_vulnerability() throws Exception {
        /* prepare */
        String xml = support.loadTestFile(TestSerecoFileSupport.NETSPARKER_V1_9_1_977_XML_TESTFILE);

        /* execute */
        SerecoMetaData result = importerToTest.importResult(xml, ScanType.WEB_SCAN);
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        for (SerecoVulnerability vulnerability : vulnerabilities) {
            assertEquals(ScanType.WEB_SCAN, vulnerability.getScanType());
        }

        /* @formatter:off */
        assertVulnerabilities(vulnerabilities).
            vulnerability().
                withSeverity(SerecoSeverity.MEDIUM).
                isExactDefinedWebVulnerability().
                    withTarget("https://app.example.org:8082/").
                and().
                withType("InvalidSslCertificate").
                classifiedBy().
                    owasp("A6").
                    wasc("4").
                    cwe("295").
                    capec("459").
                    pci32("6.5.4").
                    and().
                withDescriptionContaining("Netsparker Enterprise identified an invalid SSL certificate.\n\n" +
                        "An SSL certificate can be created and signed by anyone. You should have a valid SSL certificate to make your visitors sure about the secure communication between your website and them. If you have an invalid certificate, your visitors will have trouble distinguishing between your certificate and those of attackers.").
                isContained().

           vulnerability().
                enableTrace().
                withSeverity(SerecoSeverity.MEDIUM).
                isExactDefinedWebVulnerability().
                    withTarget("http://app.example.org:8082/").
                and().
                withType("InsecureHttpUsage").
                classifiedBy().
                    owasp("A5").
                    wasc("4").
                    and().
                withDescriptionContaining("Netsparker Enterprise identified that the target website allows web browsers to access to the website over HTTP and doesn't redirect them to HTTPS.\n\n" +
                        "HSTS is implemented in the target website however HTTP requests are not redirected to HTTPS. This decreases the value of HSTS implementation significantly.\n\n" +
                        "For example visitors who haven't visited the HTTPS version of the website previously will not be able to take advantage of HSTS.").
                isContained();
        /* @formatter:on */

    }

    @Test
    void test_xml_import_netsparker_1_9_1_977_contains_specific_vulnerability_with_table() throws Exception {
        /* prepare */
        String xml = support.loadTestFile(TestSerecoFileSupport.NETSPARKER_V1_9_1_977_XML_TESTFILE);

        /* execute */
        SerecoMetaData result = importerToTest.importResult(xml, ScanType.WEB_SCAN);
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        for (SerecoVulnerability vulnerability : vulnerabilities) {
            assertEquals(ScanType.WEB_SCAN, vulnerability.getScanType());
        }

        /* @formatter:off */
        assertVulnerabilities(vulnerabilities).
            vulnerability().
                withSeverity(SerecoSeverity.MEDIUM).
                isExactDefinedWebVulnerability().
                    withTarget("https://app.example.org:8082/").
                and().
                withType("HstsErrors").
                classifiedBy().
                    owasp("A5").
                    wasc("15").
                    cwe("16").
                    and().
                withDescriptionContaining("Netsparker Enterprise detected errors during parsing of Strict-Transport-Security header.\n\n" +
                        ".Table\n" +
                        "|=========================\n" +
                        "| Error | Resolution\n" +
                        "\n" +
                        "| preload directive not present\n" +
                        "| Submit domain for inclusion in browsers' HTTP Strict Transport Security (HSTS) preload list.\n" +
                        "|=========================").
                isContained();
        /* @formatter:on */

    }
}
