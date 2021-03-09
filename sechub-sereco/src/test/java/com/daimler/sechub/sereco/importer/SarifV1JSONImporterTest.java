// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

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
    
    @BeforeClass
    public static void before() {
        importerToTest = new SarifV1JSONImporter();
        sarifBrakeman = SerecoTestFileSupport.INSTANCE.loadTestFile("sarif/sarif_2.1.0_brakeman.json");
        sarifEmptyResult = SerecoTestFileSupport.INSTANCE.loadTestFile("sarif/sarif_2.1.0_empty_results.json");
        sarifThreadflowsExample = SerecoTestFileSupport.INSTANCE.loadTestFile("sarif/sarif_2.1.0_threadflows_example.json");
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
            importerToTest.importResult("");// here we call the importer directly with empty string, isAbleToImport is not used, so an exception is expected
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
        assertEquals("Cross-Site Scripting", vulnerability.getType());
        assertEquals("Gemfile.lock", codeInfo.getLocation());
        assertEquals(115, codeInfo.getLine().intValue());
        assertEquals(21, vulnerabilities.size());
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

    private SerecoVulnerability fetchFirstNonFalsePositive(List<SerecoVulnerability> vulnerabilities) {
        Iterator<SerecoVulnerability> vit = vulnerabilities.iterator();
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
