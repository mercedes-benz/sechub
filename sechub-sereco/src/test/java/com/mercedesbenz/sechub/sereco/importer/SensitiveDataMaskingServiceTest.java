// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.test.TestFileReader;

class SensitiveDataMaskingServiceTest {

    private static final String SECHUB_WEBSCAN_CONFIG_FILE_WITH_SENSITIVE_HEADERS = "src/test/resources/sechub-config-examples/sechub_webscan_config_with_sensitive_headers.json";
    private static final String SECHUB_WEBSCAN_CONFIG_FILE_WITHOUT_SENSITIVE_HEADERS = "src/test/resources/sechub-config-examples/sechub_webscan_config_without_sensitive_headers.json";

    private static final String SARIF_2_1_0_OWASP_ZAP_REPORT_FILE_WITH_SENSITIVE_HEADERS = "src/test/resources/sarif/sarif_2.1.0_owasp_zap_with_sensitive_headers.json";
    private static final String SARIF_2_1_0_OWASP_ZAP_REPORT_FILE_WITHOUT_SENSITIVE_HEADERS = "src/test/resources/sarif/sarif_2.1.0_owasp_zap_without_sensitive_headers.json";
    private static final String SARIF_2_1_0_OWASP_ZAP_EMPTY_REPORT_FILE = "src/test/resources/sarif/sarif_2.1.0_owasp_zap_empty_report.json";

    private SensitiveDataMaskingService serviceToTest;

    private SarifV1JSONImporter importer;

    @BeforeEach
    void beforeEach() {
        importer = new SarifV1JSONImporter();
        serviceToTest = new SensitiveDataMaskingService();
    }

    @Test
    void sensitive_headers_are_filtered_case_insensitive() throws IOException {
        /* prepare */
        String sechubConfigJson = TestFileReader.loadTextFile(SECHUB_WEBSCAN_CONFIG_FILE_WITH_SENSITIVE_HEADERS);
        SecHubConfigurationModel scanConfigWithSensitiveHeaders = SecHubConfiguration.createFromJSON(sechubConfigJson);

        String sarifReport = TestFileReader.loadTextFile(SARIF_2_1_0_OWASP_ZAP_REPORT_FILE_WITH_SENSITIVE_HEADERS);
        SerecoMetaData data = importer.importResult(sarifReport, ScanType.WEB_SCAN);

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfigWithSensitiveHeaders, data.getVulnerabilities());

        /* test */
        assertEquals(14, maskedVulnerabilities.size());
        assertHeaderWasMasked("authorization", maskedVulnerabilities);
        assertHeaderWasMasked("Api-Key", maskedVulnerabilities);
        assertHeaderWasNotMasked("x-file-size", "123456", maskedVulnerabilities);
    }

    @Test
    void non_sensitive_headers_are_not_changed() throws IOException {
        /* prepare */
        String sechubConfigJson = TestFileReader.loadTextFile(SECHUB_WEBSCAN_CONFIG_FILE_WITHOUT_SENSITIVE_HEADERS);
        SecHubConfigurationModel scanConfigWithoutSensitiveHeaders = SecHubConfiguration.createFromJSON(sechubConfigJson);

        String sarifReport = TestFileReader.loadTextFile(SARIF_2_1_0_OWASP_ZAP_REPORT_FILE_WITHOUT_SENSITIVE_HEADERS);
        SerecoMetaData data = importer.importResult(sarifReport, ScanType.WEB_SCAN);

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfigWithoutSensitiveHeaders, data.getVulnerabilities());

        /* test */
        assertEquals(14, maskedVulnerabilities.size());
        assertHeaderWasNotMasked("x-file-size", "123456", maskedVulnerabilities);
        assertHeaderWasNotMasked("custom-header", "non-secret-value", maskedVulnerabilities);
    }

    @Test
    void vulnerabilities_not_changed_when_no_web_scan_config_is_defined() throws IOException {
        /* prepare */
        String sarifReport = TestFileReader.loadTextFile(SARIF_2_1_0_OWASP_ZAP_REPORT_FILE_WITH_SENSITIVE_HEADERS);
        SerecoMetaData data = importer.importResult(sarifReport, ScanType.WEB_SCAN);
        SecHubConfigurationModel sechubConfigWithoutWebScan = new SecHubConfigurationModel();

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(sechubConfigWithoutWebScan, data.getVulnerabilities());

        /* test */
        assertEquals(data.getVulnerabilities(), maskedVulnerabilities);
    }

    @Test
    void vulnerabilities_not_changed_when_no_headers_are_configured() throws IOException {
        /* prepare */
        SecHubConfigurationModel scanConfigWithoutHeaders = new SecHubConfigurationModel();
        scanConfigWithoutHeaders.setWebScan(new SecHubWebScanConfiguration());

        String sarifReport = TestFileReader.loadTextFile(SARIF_2_1_0_OWASP_ZAP_REPORT_FILE_WITH_SENSITIVE_HEADERS);
        SerecoMetaData data = importer.importResult(sarifReport, ScanType.WEB_SCAN);

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfigWithoutHeaders, data.getVulnerabilities());

        /* test */
        assertEquals(data.getVulnerabilities(), maskedVulnerabilities);
    }

    @Test
    void empty_sarif_report_with_no_findings_handled_without_changing_anything() throws IOException {
        /* prepare */
        String sechubConfigJson = TestFileReader.loadTextFile(SECHUB_WEBSCAN_CONFIG_FILE_WITH_SENSITIVE_HEADERS);
        SecHubConfigurationModel scanConfig = SecHubConfiguration.createFromJSON(sechubConfigJson);

        String sarifReport = TestFileReader.loadTextFile(SARIF_2_1_0_OWASP_ZAP_EMPTY_REPORT_FILE);
        SerecoMetaData data = importer.importResult(sarifReport, ScanType.WEB_SCAN);

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfig, data.getVulnerabilities());

        /* test */
        assertEquals(data.getVulnerabilities(), maskedVulnerabilities);
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class, names = { "WEB_SCAN" }, mode = EnumSource.Mode.EXCLUDE)
    void wrong_scan_type_results_in_unchanged_vulnerabilities(ScanType scanType) {
        /* prepare */
        String sechubConfigJson = TestFileReader.loadTextFile(SECHUB_WEBSCAN_CONFIG_FILE_WITH_SENSITIVE_HEADERS);
        SecHubConfigurationModel scanConfigWithSensitiveHeaders = SecHubConfiguration.createFromJSON(sechubConfigJson);

        SerecoVulnerability vulnerability = mock(SerecoVulnerability.class);
        vulnerability.setWeb(new SerecoWeb());
        SerecoMetaData data = new SerecoMetaData();
        data.getVulnerabilities().add(vulnerability);

        when(vulnerability.getScanType()).thenReturn(scanType);

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfigWithSensitiveHeaders, data.getVulnerabilities());

        /* test */
        assertEquals(data.getVulnerabilities(), maskedVulnerabilities);
        verify(vulnerability, times(1)).getScanType();
        verify(vulnerability, never()).getWeb();
    }

    @Test
    void empty_web_part_results_in_unchanged_vulnerabilities() {
        /* prepare */
        String sechubConfigJson = TestFileReader.loadTextFile(SECHUB_WEBSCAN_CONFIG_FILE_WITH_SENSITIVE_HEADERS);
        SecHubConfigurationModel scanConfigWithSensitiveHeaders = SecHubConfiguration.createFromJSON(sechubConfigJson);

        SerecoVulnerability vulnerability = mock(SerecoVulnerability.class);
        vulnerability.setWeb(null);
        SerecoMetaData data = new SerecoMetaData();
        data.getVulnerabilities().add(vulnerability);

        when(vulnerability.getScanType()).thenReturn(ScanType.WEB_SCAN);

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfigWithSensitiveHeaders, data.getVulnerabilities());

        /* test */
        assertEquals(data.getVulnerabilities(), maskedVulnerabilities);
        verify(vulnerability, times(1)).getScanType();
        verify(vulnerability, times(1)).getWeb();
    }

    private void assertHeaderWasMasked(String headerName, List<SerecoVulnerability> maskedVulnerabilities) {
        for (SerecoVulnerability vulnerability : maskedVulnerabilities) {
            Map<String, String> requestHeaders = vulnerability.getWeb().getRequest().getHeaders();
            Map<String, String> responseHeaders = vulnerability.getWeb().getResponse().getHeaders();

            if (requestHeaders.containsKey(headerName)) {
                assertEquals(SensitiveDataMaskingService.SENSITIVE_DATA_MASK, requestHeaders.get(headerName));
            }
            if (responseHeaders.containsKey(headerName)) {
                assertEquals(SensitiveDataMaskingService.SENSITIVE_DATA_MASK, responseHeaders.get(headerName));
            }
        }
    }

    private void assertHeaderWasNotMasked(String headerName, String headerValue, List<SerecoVulnerability> maskedVulnerabilities) {
        for (SerecoVulnerability vulnerability : maskedVulnerabilities) {
            Map<String, String> requestHeaders = vulnerability.getWeb().getRequest().getHeaders();
            Map<String, String> responseHeaders = vulnerability.getWeb().getResponse().getHeaders();

            if (requestHeaders.containsKey(headerName)) {
                String actualHeaderValue = requestHeaders.get(headerName);
                if (actualHeaderValue.equalsIgnoreCase(headerValue) == false) {
                    fail("For header: '" + headerName + "' expected value: '" + headerValue + "', but got '" + actualHeaderValue + "' instead!");
                }
            }
            if (responseHeaders.containsKey(headerName)) {
                String actualHeaderValue = responseHeaders.get(headerName);
                if (actualHeaderValue.equalsIgnoreCase(headerValue) == false) {
                    fail("For header: '" + headerName + "' expected value: '" + headerValue + "', but got '" + actualHeaderValue + "' instead!");
                }
            }
        }
    }

}
