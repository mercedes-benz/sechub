// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static com.mercedesbenz.sechub.sereco.test.AssertVulnerabilities.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebRequest;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebResponse;
import com.mercedesbenz.sechub.sereco.test.SerecoTestFileSupport;

class SarifV1JSONImporterTest {

    private static String sarif_2_1_0_brakeman;
    private static String sarif_2_1_0_es_lint_empty_results;
    private static String sarif_2_1_0_pythonscanner_thread_flows;
    private static String sarif_2_1_0_gosec2_8_0_taxonomyExample;
    private static String sarif_2_1_0_coverity_20_21_03_taxonomyExample;
    private static String sarif_2_1_0_owasp_zap;
    private static String sarif_2_1_0_gosec2_9_5_example5_cosdescan;
    private static String sarif_2_1_0_sarif_2_1_0_gitleaks_8_0;

    private SarifV1JSONImporter importerToTest;

    @BeforeAll
    public static void before() {
        sarif_2_1_0_brakeman = loadSarifTestFile("sarif_2.1.0_brakeman.json");
        sarif_2_1_0_es_lint_empty_results = loadSarifTestFile("sarif_2.1.0_empty_results.json");
        sarif_2_1_0_pythonscanner_thread_flows = loadSarifTestFile("sarif_2.1.0_threadflows_example.json");
        sarif_2_1_0_gosec2_8_0_taxonomyExample = loadSarifTestFile("sarif_2.1.0_gosec_2.8.0_example_with_taxonomy.json");
        sarif_2_1_0_gosec2_9_5_example5_cosdescan = loadSarifTestFile("sarif_2.1.0_gosec_2.9.5_example5_codescan.sarif.json");
        sarif_2_1_0_coverity_20_21_03_taxonomyExample = loadSarifTestFile("sarif_2.1.0_coverity_20.21.03_example_with_taxonomy.json");
        sarif_2_1_0_owasp_zap = loadSarifTestFile("sarif_2.1.0_owasp_zap.json");
        sarif_2_1_0_sarif_2_1_0_gitleaks_8_0 = loadSarifTestFile("sarif_2.1.0_gitleaks_8.0.json");
    }

    @BeforeEach
    void beforeEach() {
        importerToTest = new SarifV1JSONImporter();

        // initialize workarounds
        importerToTest.workaroundSupport = new SarifImportProductWorkaroundSupport();
    }

    @Test
    void sarif_2_1_0_owasp_zap_can_be_imported_and_contains_cwe_and_is_marked_as_webscan_type() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_owasp_zap, ScanType.WEB_SCAN);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        /* @formatter:off */
        assertVulnerabilities(vulnerabilities).
            hasVulnerabilities(14).
            verifyVulnerability().
                classifiedBy().
                    cwe(79).
                    and().
                withSeverity(SerecoSeverity.HIGH).
                withType("Cross Site Scripting (Reflected)").
                withScanType(ScanType.WEB_SCAN).

            isContained();

        /* @formatter:on */
    }

    @Test
    void sarif_2_1_0_owasp_zap_can_be_imported_and_contains_cwe_webvulnerability_with_parts() throws Exception {
        /* @formatter:off */
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_owasp_zap, ScanType.WEB_SCAN);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        SerecoWebRequest expectedRequest = new SerecoWebRequest();
        expectedRequest.setMethod("GET");
        expectedRequest.setProtocol("HTTP");
        expectedRequest.setVersion("1.1");
        expectedRequest.setTarget("https://127.0.0.1:8080/greeting?name=%3C%2Fp%3E%3Cscript%3Ealert%281%29%3B%3C%2Fscript%3E%3Cp%3E");

        Map<String, String> requestHeaders = expectedRequest.getHeaders();
        requestHeaders.put("Cache-Control","no-cache");
        requestHeaders.put("Content-Length","0");
        requestHeaders.put("Cookie","JSESSIONID=38AA1F7A61982DF1073D7F43A3707798; locale=de");
        requestHeaders.put("Host","127.0.0.1:8080");
        requestHeaders.put("Pragma","no-cache");
        requestHeaders.put("Referer","https://127.0.0.1:8080/hello");
        requestHeaders.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:92.0) Gecko/20100101 Firefox/92.0");

        SerecoWebResponse expectedResponse = new SerecoWebResponse();
        expectedResponse.setStatusCode(200);
        expectedResponse.getBody().setText("<!DOCTYPE HTML>\n"
                + "<html>\n"
                + "<head>\n"
                + "    <title>Getting Started: Serving Web Content</title>\n"
                + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                + "</head>\n"
                + "<body>\n"
                + "    <!-- unsecure text used (th:utext instead th:text)- to create vulnerability (XSS) -->\n"
                + "    <!-- simple usage: http://localhost:8080/greeting?name=Test2</p><script>;alert(\"hallo\")</script> -->\n"
                + "    <p >XSS attackable parameter output: </p><script>alert(1);</script><p>!</p>\n"
                + "</body>\n"
                + "</html>");

        Map<String, String> responseHeaders = expectedResponse.getHeaders();
        responseHeaders.put("Cache-Control","no-cache, no-store, max-age=0, must-revalidate");
        responseHeaders.put("Content-Language","en-US");
        responseHeaders.put("Content-Security-Policy","script-src 'self'");
        responseHeaders.put("Content-Type","text/html;charset=UTF-8");
        responseHeaders.put("Date","Thu, 11 Nov 2021 09:56:20 GMT");
        responseHeaders.put("Expires","0");
        responseHeaders.put("Pragma","no-cache");
        responseHeaders.put("Referrer-Policy","no-referrer");
        responseHeaders.put("Set-Cookie","locale=de; HttpOnly; SameSite=strict");
        responseHeaders.put("Strict-Transport-Security","max-age=31536000 ; includeSubDomains");
        responseHeaders.put("X-Content-Type-Options","nosniff");
        responseHeaders.put("X-Frame-Options","DENY");
        responseHeaders.put("X-XSS-Protection","1; mode=block");

        expectedResponse.setProtocol("HTTP");
        expectedResponse.setVersion("1.1");
        expectedResponse.setStatusCode(200);
        expectedResponse.setReasonPhrase("");

        SerecoVulnerability firstCSSvulnerability = assertVulnerabilities(vulnerabilities).
            hasVulnerabilities(14).
            verifyVulnerability().
                classifiedBy().
                    cwe(79).
                    and().
                withSeverity(SerecoSeverity.HIGH).
                withType("Cross Site Scripting (Reflected)").
                withScanType(ScanType.WEB_SCAN).
            assertContainedAndReturn();

        assertWebRequest(firstCSSvulnerability, expectedRequest);
        assertWebResponse(firstCSSvulnerability, expectedResponse);

        assertVulnerabilities(vulnerabilities).
            verifyVulnerability().
                classifiedBy().
                    cwe(79).
                    and().
                withSeverity(SerecoSeverity.HIGH).
                withType("Cross Site Scripting (Reflected)").
                withScanType(ScanType.WEB_SCAN).
                isExactDefinedWebVulnerability().
                    withWebRequest(expectedRequest).
                    withWebResponse(expectedResponse).
            isContained();

        /* @formatter:on */
    }

    @Test
    void sarif_2_1_0_coverity_v8_can_be_imported_and_contains_cwe_with_description() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_coverity_20_21_03_taxonomyExample, ScanType.CODE_SCAN);

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
                withScanType(ScanType.CODE_SCAN).
                withCodeLocation("securibench-micro/src/securibench/micro/aliasing/Aliasing1.java",45,0).
                    calling("securibench-micro/src/securibench/micro/aliasing/Aliasing1.java",45,0).
                    calling("securibench-micro/src/securibench/micro/aliasing/Aliasing1.java",41,0).
                    calling("securibench-micro/src/securibench/micro/aliasing/Aliasing1.java",45,0).
                    done().
                withType("Cross-site scripting").
                withDescriptionContaining("XSS: Printing \"str\" to an HTML page allows cross-site scripting, because it was not properly sanitized for context HTML PCDATA block.\n"
                        + "Remediation Advice: Perform the following escaping in the following order to guard against cross-site scripting attacks with Java.\n"
                        + "\n"
                        + "For example: \"Escape.html(str)\"").
            isContained();

        /* @formatter:on */
    }

    @Test
    void go_sec_2_8_0_example_with_taxonomy__import_ability_is_true() {
        /* prepare */

        ImportParameter paramGoSec = ImportParameter.builder().importData(sarif_2_1_0_gosec2_8_0_taxonomyExample).importId("id1").productId("PDS_CODESCAN")
                .build();

        /* execute */
        boolean ableToImportGosec_2_8_0sarif = importerToTest.isAbleToImportForProduct(paramGoSec);

        /* test */
        assertTrue(ableToImportGosec_2_8_0sarif, "Has NOT the ability to import sarif!");
    }

    @Test
    void go_sec_2_8_0_example_with_taxonomy__can_be_imported_and_contains_cwe_with_description() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_gosec2_8_0_taxonomyExample, ScanType.CODE_SCAN);

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
    void go_sec_2_9_5_example5_codescan__can_be_imported_and_contains_source() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_gosec2_9_5_example5_cosdescan, ScanType.CODE_SCAN);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        /* @formatter:off */
        assertVulnerabilities(vulnerabilities).
            hasVulnerabilities(38).
            verifyVulnerability().
                classifiedBy().
                    cwe(79).
                    and().
                withDescriptionContaining("will not auto-escape").
                withSeverity(SerecoSeverity.HIGH).
                withCodeLocation("go-test-bench/pkg/servestd/servestd.go", 69,14).containingSource("68: \t\t}\n69: \t\tvar data = template.HTML(v.TmplFile)\n70: \t\tisTmpl := true").done().
            isContained();

        /* @formatter:on */
    }

    @Test
    void gitleaks_8_0_example_secretscan__can_be_imported() throws Exception {
        /* prepare */
        importerToTest.workaroundSupport.workarounds.add(new GitleaksSarifImportWorkaround());
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_sarif_2_1_0_gitleaks_8_0, ScanType.SECRET_SCAN);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        /* @formatter:off */
        assertVulnerabilities(vulnerabilities).
            hasVulnerabilities(6).
            verifyVulnerability().
                classifiedBy().cwe(798).and(). // 798 is our generic fallback for secret scans when no cweId is set by products (gitleaks SARIF does currently not set cweId)
                withDescriptionContaining("generic-api-key has detected secret for file UnSAFE_Bank/Backend/src/api/application/config/database.php.").
                withType("Generic API Key").
                withCodeLocation("UnSAFE_Bank/Backend/src/api/application/config/database.php", 80, 7).containingSource("531486b2bf646636a6a1bba61e78ec4a4a54efbd").done().
            isContained();

        /* @formatter:on */
    }

    @Test
    void brakeman_sarif_report_can_be_imported() {
        /* prepare */

        ImportParameter paramBrakeman = ImportParameter.builder().importData(sarif_2_1_0_brakeman).importId("id1").productId("PDS_CODESCAN").build();

        /* execute */
        boolean ableToImportBrakemanSarif = importerToTest.isAbleToImportForProduct(paramBrakeman);

        /* test */
        assertTrue(ableToImportBrakemanSarif, "Was NOT able to import brakeman sarif!");
    }

    @Test
    void threadflow_sarif_report_can_be_imported() {
        /* prepare */
        ImportParameter paramThreadFlows = ImportParameter.builder().importData(sarif_2_1_0_pythonscanner_thread_flows).importId("id1")
                .productId("PDS_CODESCAN").build();

        /* execute */
        boolean ableToImportThreadFlowSarif = importerToTest.isAbleToImportForProduct(paramThreadFlows);

        /* test */
        assertTrue(ableToImportThreadFlowSarif, "Was NOT able to import thread flow sarif!");
    }

    @Test
    void empty_json__can_NOT_be_imported() {
        /* prepare */

        ImportParameter emptyJSONImportParam = ImportParameter.builder().importData("{}").importId("id1").productId("PDS_CODESCAN").build();

        /* execute */
        boolean importAbility = importerToTest.isAbleToImportForProduct(emptyJSONImportParam);

        /* test */
        assertFalse(importAbility, "Empty json should be importable");
    }

    @Test
    void empty_string_is_recognized_as_product_failure() {
        /* prepare */

        ImportParameter emptyJSONImportParam = ImportParameter.builder().importData("").importId("id1").productId("PDS_CODESCAN").build();

        /* execute */
        boolean importAbility = importerToTest.isAbleToImportForProduct(emptyJSONImportParam);

        /* test */
        assertFalse(importAbility, "Empty string should be importable");
    }

    @Test
    void empty_sarif_report_throws_exception() {

        /* test */
        assertThrows(IOException.class, () -> {
            importerToTest.importResult("", ScanType.CODE_SCAN);// here we call the importer directly with empty string, isAbleToImport is not
            // used, so an exception is expected
        });
    }

    @Test
    void null_sarif_report_throws_exception() {

        /* test */
        assertThrows(IOException.class, () -> {
            importerToTest.importResult(null, null);
        });
    }

    @Test
    void sarif_report_has_errorlevel() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_brakeman, ScanType.CODE_SCAN);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        SerecoVulnerability vulnerability = vulnerabilities.get(0);

        /* test */
        assertEquals(SerecoSeverity.HIGH, vulnerability.getSeverity());
    }

    @Test
    void sarif_report_has_simple_text_description() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_brakeman, ScanType.CODE_SCAN);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        SerecoVulnerability vulnerability = vulnerabilities.get(0);

        /* test */
        assertEquals("Rails 5.0.0 `content_tag` does not escape double quotes in attribute values (CVE-2016-6316). Upgrade to Rails 5.0.0.1.",
                vulnerability.getDescription());
    }

    @Test
    void sarif_report_has_no_results() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_es_lint_empty_results, ScanType.CODE_SCAN);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();

        /* test */
        assertTrue(vulnerabilities.isEmpty());

    }

    @Test
    void sarif_report_has_code_info() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_brakeman, ScanType.CODE_SCAN);

        /* execute */
        List<SerecoVulnerability> vulnerabilities = result.getVulnerabilities();
        SerecoVulnerability vulnerability = fetchFirstNonFalsePositive(vulnerabilities);
        SerecoCodeCallStackElement codeInfo = vulnerability.getCode();

        /* test */
        assertNotNull(codeInfo);
        assertEquals("BRAKE0102", vulnerability.getType()); // brakeman does not provide a short description, so fallback to id (which must
                                                            // be available)
        assertEquals("Rails 5.0.0 `content_tag` does not escape double quotes in attribute values (CVE-2016-6316). Upgrade to Rails 5.0.0.1.",
                vulnerability.getDescription());
        assertEquals("Gemfile.lock", codeInfo.getLocation());
        assertEquals(115, codeInfo.getLine().intValue());
        assertEquals(32, vulnerabilities.size());
    }

    @Test
    void sarif_report_threadflow_locations() throws Exception {
        /* prepare */
        SerecoMetaData result = importerToTest.importResult(sarif_2_1_0_pythonscanner_thread_flows, ScanType.CODE_SCAN);

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
