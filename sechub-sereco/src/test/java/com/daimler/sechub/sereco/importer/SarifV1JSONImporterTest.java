// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import static com.daimler.sechub.sereco.test.AssertVulnerabilities.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.daimler.sechub.sereco.test.SerecoTestFileSupport;

public class SarifV1JSONImporterTest {

    private static SarifV1JSONImporter importerToTest;
    private static String sarifBrakeman;
    private static String sarifEmptyResult;
    private static String sarifThreadflowsExample;
    private static String gosec2_8_0_taxonomyExample;
    private static String sarif_2_1_0_coverity_v8;

    @BeforeClass
    public static void before() {
        importerToTest = new SarifV1JSONImporter();
        sarifBrakeman = loadSarifTestFile("sarif_2.1.0_brakeman.json");
        sarifEmptyResult = loadSarifTestFile("sarif_2.1.0_empty_results.json");
        sarifThreadflowsExample = loadSarifTestFile("sarif_2.1.0_threadflows_example.json");

        gosec2_8_0_taxonomyExample = loadSarifTestFile("sarif_2.1.0_gosec_2.8.0_example_with_taxonomy.json");
        
        sarif_2_1_0_coverity_v8 = loadSarifTestFile("sarif_2.1.0_coverity_20.21.03_example_with_taxonomy.json");    }

    @Test
    public void sarif_2_1_0_coverity_v8_can_be_imported_and_contains_cwe_with_description() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_coverity_v8);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        /* @formatter:off */
        assertVulnerabilities(vulnerabilities).
            hasVulnerabilities(189).
            verifyVulnerability().
                classifiedBy().
                    cwe(79).
                    and().
                withSeverity(SerecoSeverity.HIGH).
                withCodeLocation("securibench-micro/src/securibench/micro/aliasing/Aliasing1.java",45,0).
                    calling("securibench-micro/src/securibench/micro/aliasing/Aliasing1.java",45,0).
                    calling("securibench-micro/src/securibench/micro/aliasing/Aliasing1.java",41,0).
                    calling("securibench-micro/src/securibench/micro/aliasing/Aliasing1.java",45,0).
                    done().
                withType("Cross-site scripting").
                withDescriptionContaining("Untrusted user-supplied data").
            isContained();
        
        /* @formatter:on */
    }
    
    @Test
    public void go_sec_2_8_0_example_with_taxonomy__import_ability_is_true() {
        /* prepare */

        ImportParameter paramGoSec = ImportParameter.builder().importData(gosec2_8_0_taxonomyExample).importId("id1").productId("PDS_CODESCAN").build();

        /* execute */
        ProductImportAbility ableToImportGosec_2_8_0sarif = importerToTest.isAbleToImportForProduct(paramGoSec);

        /* test */
        assertEquals("Has NOT the ability to import sarif!", ProductImportAbility.ABLE_TO_IMPORT, ableToImportGosec_2_8_0sarif);
    }

    @Test
    public void go_sec_2_8_0_example_with_taxonomy__can_be_imported_and_contains_cwe_with_description() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(gosec2_8_0_taxonomyExample);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        /* @formatter:off */
        assertVulnerabilities(vulnerabilities).
            hasVulnerabilities(1).
            verifyVulnerability().
                classifiedBy().
                    cwe(89).
                    and().
                withSeverity(SerecoSeverity.HIGH).
                withDescriptionContaining("SQL string formatting").
            isContained();
        
        /* @formatter:on */
    }

    @Test
    public void brakeman_sarif_report_can_be_imported() {
        /* prepare */

        ImportParameter paramBrakeman = ImportParameter.builder().importData(sarifBrakeman).importId("id1").productId("PDS_CODESCAN").build();

        /* execute */
        ProductImportAbility ableToImportBrakemanSarif = importerToTest.isAbleToImportForProduct(paramBrakeman);

        /* test */
        assertEquals("Was NOT able to import sarif!", ProductImportAbility.ABLE_TO_IMPORT, ableToImportBrakemanSarif);
    }

    @Test
    public void threadflow_sarif_report_can_be_imported() {
        /* prepare */
        ImportParameter paramThreadFlows = ImportParameter.builder().importData(sarifThreadflowsExample).importId("id1").productId("PDS_CODESCAN").build();

        /* execute */
        ProductImportAbility ableToImportThreadFlowSarif = importerToTest.isAbleToImportForProduct(paramThreadFlows);

        /* test */
        assertEquals("Was NOT able to import sarif!", ProductImportAbility.ABLE_TO_IMPORT, ableToImportThreadFlowSarif);
    }

    @Test
    public void empty_json__can_NOT_be_imported() {
        /* prepare */

        ImportParameter emptyJSONImportParam = ImportParameter.builder().importData("{}").importId("id1").productId("PDS_CODESCAN").build();

        /* execute */
        ProductImportAbility importAbility = importerToTest.isAbleToImportForProduct(emptyJSONImportParam);

        /* test */
        assertEquals("Not the expected ability!", ProductImportAbility.NOT_ABLE_TO_IMPORT, importAbility);
    }

    @Test
    public void empty_string_is_recognized_as_product_failure() {
        /* prepare */

        ImportParameter emptyJSONImportParam = ImportParameter.builder().importData("").importId("id1").productId("PDS_CODESCAN").build();

        /* execute */
        ProductImportAbility importAbility = importerToTest.isAbleToImportForProduct(emptyJSONImportParam);

        /* test */
        assertEquals("Not the expected ability!", ProductImportAbility.PRODUCT_FAILED, importAbility);
    }

    @Test
    public void empty_sarif_report_throws_exception() {

        /* test */
        assertThrows(IOException.class, () -> {
            importerToTest.importResult("");// here we call the importer directly with empty string, isAbleToImport is not
                                            // used, so an exception is expected
        });
    }

    @Test
    public void null_sarif_report_throws_exception() {

        /* test */
        assertThrows(IOException.class, () -> {
            importerToTest.importResult(null);
        });
    }

    @Test
    public void sarif_report_has_errorlevel() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarifBrakeman);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        SerecoVulnerability vulnerability = vulnerabilities.get(0);

        /* test */
        assertEquals(SerecoSeverity.HIGH, vulnerability.getSeverity());
    }

    @Test
    public void sarif_report_has_simple_text_description() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarifBrakeman);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        SerecoVulnerability vulnerability = vulnerabilities.get(0);

        /* test */
        assertEquals("Checks for XSS in calls to content_tag.", vulnerability.getDescription());
    }

    @Test
    public void sarif_report_has_no_results() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarifEmptyResult);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        assertTrue(vulnerabilities.isEmpty());

    }

    @Test
    public void sarif_report_has_code_info() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarifBrakeman);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        SerecoVulnerability vulnerability = fetchFirstNonFalsePositive(vulnerabilities);
        SerecoCodeCallStackElement codeInfo = vulnerability.getCode();

        /* test */
        assertNotNull(codeInfo);
        assertEquals("BRAKE0102", vulnerability.getType()); //brakeman does not provide a short description, so fallback to id (which must be available) 
        assertEquals("Checks for XSS in calls to content_tag.",vulnerability.getDescription());
        assertEquals("Gemfile.lock", codeInfo.getLocation());
        assertEquals(115, codeInfo.getLine().intValue());
        assertEquals(32, vulnerabilities.size());
    }

    @Test
    public void sarif_report_threadflow_locations() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarifThreadflowsExample);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        SerecoVulnerability vulnerability = fetchFirstNonFalsePositive(vulnerabilities);
        SerecoCodeCallStackElement codeInfo = vulnerability.getCode();

        /* test */
        assertEquals("Undefined", vulnerability.getType()); // was not able to detect from this data
        assertNotNull(codeInfo);
        assertEquals("3-Beyond-basics/bad-eval-with-code-flow.py", codeInfo.getLocation());
        assertEquals(3, codeInfo.getLine().intValue());
        assertEquals(1, vulnerabilities.size());

        SerecoCodeCallStackElement subCodeInfo = codeInfo.getCalls();
        assertNotNull(subCodeInfo);
        assertEquals("3-Beyond-basics/bad-eval-with-code-flow.py", subCodeInfo.getLocation());
        assertEquals(4, subCodeInfo.getLine().intValue());
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helpers......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private static String loadSarifTestFile(String sarifTestFile) {
        return SerecoTestFileSupport.INSTANCE.loadTestFile("sarif/" + sarifTestFile);
    }

    private SerecoVulnerability fetchFirstNonFalsePositive(List<SerecoVulnerability> vulnerabilities) {
        Iterator<SerecoVulnerability> vit = vulnerabilities.iterator();
        if (!vit.hasNext()) {
            fail("no vulnerability found at all!");
        }
        SerecoVulnerability vulnerability = vit.next();
        while (vulnerability.isFalsePositive()) {
            /*
             * we have also false positives here .. and just search for first non false
             * Positive
             */
            vulnerability = vit.next();
        }
        return vulnerability;
    }

}
