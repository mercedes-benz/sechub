// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.test.TestFileReader;

class SensitiveDataMaskingServiceTest {

    private static final String SECHUB_WEBSCAN_CONFIG_FILE_WITH_SENSITIVE_HEADERS = "src/test/resources/sechub-config-examples/sechub_webscan_config_with_sensitive_headers.json";
    private static final String SECHUB_WEBSCAN_CONFIG_FILE_WITHOUT_SENSITIVE_HEADERS = "src/test/resources/sechub-config-examples/sechub_webscan_config_without_sensitive_headers.json";

    private SensitiveDataMaskingService serviceToTest;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new SensitiveDataMaskingService();
    }

    @Test
    void sechub_scan_config_is_null_results_in_illegal_argument_exception() {
        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> serviceToTest.maskSensitiveData(null, new ArrayList<>()));

        /* test */
        assertEquals("Cannot mask sensitive data because the sechub configuration was null!", exception.getMessage());
    }

    @Test
    void list_of_sereco_vulnerabilities_is_null_results_in_illegal_argument_exception() {
        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> serviceToTest.maskSensitiveData(new SecHubConfigurationModel(), null));

        /* test */
        assertEquals("Cannot mask sensitive data because the list of sereco vulnerabilities is null!", exception.getMessage());
    }

    @Test
    void sensitive_headers_are_filtered_case_insensitive() throws IOException {
        /* prepare */
        SecHubConfigurationModel scanConfigWithSensitiveHeaders = createConfigWithSensitive();
        List<SerecoVulnerability> vulnerabilities = createListOfVulnerabilitiesWithSensitiveHeaders();

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfigWithSensitiveHeaders, vulnerabilities);

        /* test */
        assertEquals(vulnerabilities.size(), maskedVulnerabilities.size());
        assertHeaderWasMasked("authorization", maskedVulnerabilities);
        assertHeaderWasMasked("api-key", maskedVulnerabilities);
        assertHeaderWasNotMasked("custom-header", "non-secret-value", maskedVulnerabilities);
    }

    @Test
    void non_sensitive_headers_are_not_changed() throws IOException {
        /* prepare */
        SecHubConfigurationModel scanConfigWithoutSensitiveHeaders = createConfigWithOutSensitive();
        List<SerecoVulnerability> vulnerabilities = createListOfVulnerabilitiesWithoutSensitiveHeaders();

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfigWithoutSensitiveHeaders, vulnerabilities);

        /* test */
        assertEquals(vulnerabilities.size(), maskedVulnerabilities.size());
        assertHeaderWasNotMasked("x-file-size", "123456", maskedVulnerabilities);
        assertHeaderWasNotMasked("custom-header", "non-secret-value", maskedVulnerabilities);
    }

    @Test
    void vulnerabilities_not_changed_when_no_web_scan_config_is_defined() throws IOException {
        /* prepare */
        SecHubConfigurationModel sechubConfigWithoutWebScan = new SecHubConfigurationModel();
        List<SerecoVulnerability> vulnerabilities = createListOfVulnerabilitiesWithSensitiveHeaders();

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(sechubConfigWithoutWebScan, vulnerabilities);

        /* test */
        assertEquals(vulnerabilities, maskedVulnerabilities);
    }

    @Test
    void vulnerabilities_not_changed_when_no_headers_are_configured() throws IOException {
        /* prepare */
        SecHubConfigurationModel scanConfigWithoutHeaders = new SecHubConfigurationModel();
        scanConfigWithoutHeaders.setWebScan(new SecHubWebScanConfiguration());
        List<SerecoVulnerability> vulnerabilities = createListOfVulnerabilitiesWithSensitiveHeaders();

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfigWithoutHeaders, vulnerabilities);

        /* test */
        assertEquals(vulnerabilities, maskedVulnerabilities);
    }

    @Test
    void empty_sarif_report_with_no_findings_handled_without_changing_anything() throws IOException {
        /* prepare */
        SecHubConfigurationModel scanConfig = createConfigWithSensitive();
        List<SerecoVulnerability> vulnerabilities = new ArrayList<>();

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfig, vulnerabilities);

        /* test */
        assertEquals(vulnerabilities, maskedVulnerabilities);
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class, names = { "WEB_SCAN" }, mode = EnumSource.Mode.EXCLUDE)
    void wrong_scan_type_results_in_unchanged_vulnerabilities(ScanType scanType) {
        /* prepare */
        SecHubConfigurationModel scanConfigWithSensitiveHeaders = createConfigWithSensitive();

        SerecoVulnerability vulnerability = mock(SerecoVulnerability.class);
        vulnerability.setWeb(new SerecoWeb());
        List<SerecoVulnerability> vulnerabilities = new ArrayList<>();
        vulnerabilities.add(vulnerability);

        when(vulnerability.getScanType()).thenReturn(scanType);

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfigWithSensitiveHeaders, vulnerabilities);

        /* test */
        assertEquals(vulnerabilities, maskedVulnerabilities);
        verify(vulnerability, times(1)).getScanType();
        verify(vulnerability, never()).getWeb();
    }

    @Test
    void null_web_part_results_in_unchanged_vulnerabilities() {
        /* prepare */
        SecHubConfigurationModel scanConfigWithSensitiveHeaders = createConfigWithSensitive();

        SerecoVulnerability vulnerability = mock(SerecoVulnerability.class);
        vulnerability.setWeb(null);
        List<SerecoVulnerability> vulnerabilities = new ArrayList<>();
        vulnerabilities.add(vulnerability);

        when(vulnerability.getScanType()).thenReturn(ScanType.WEB_SCAN);

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfigWithSensitiveHeaders, vulnerabilities);

        /* test */
        assertEquals(vulnerabilities, maskedVulnerabilities);
        verify(vulnerability, times(1)).getScanType();
        verify(vulnerability, times(1)).getWeb();
    }

    private List<SerecoVulnerability> createListOfVulnerabilitiesWithSensitiveHeaders() {
        List<SerecoVulnerability> vulnerabilities = new ArrayList<>();
        SerecoVulnerability serecoVuln = new SerecoVulnerability();

        serecoVuln.setScanType(ScanType.WEB_SCAN);
        SerecoWeb web = new SerecoWeb();
        web.getRequest().getHeaders().put("Authorization", "Bearer secret-token");
        web.getRequest().getHeaders().put("Api-Key", "secret-key");
        web.getRequest().getHeaders().put("Custom-Header", "non-secret-value");

        web.getResponse().getHeaders().put("Authorization", "Bearer secret-token");
        web.getResponse().getHeaders().put("Api-Key", "secret-key");
        web.getResponse().getHeaders().put("Custom-Header", "non-secret-value");
        serecoVuln.setWeb(web);

        vulnerabilities.add(serecoVuln);
        return vulnerabilities;
    }

    private List<SerecoVulnerability> createListOfVulnerabilitiesWithoutSensitiveHeaders() {
        List<SerecoVulnerability> vulnerabilities = new ArrayList<>();
        SerecoVulnerability serecoVuln = new SerecoVulnerability();

        serecoVuln.setScanType(ScanType.WEB_SCAN);
        SerecoWeb web = new SerecoWeb();
        web.getRequest().getHeaders().put("X-File-Size", "123456");
        web.getRequest().getHeaders().put("Custom-Header", "non-secret-value");

        web.getResponse().getHeaders().put("X-File-Size", "123456");
        web.getResponse().getHeaders().put("Custom-Header", "non-secret-value");
        serecoVuln.setWeb(web);

        vulnerabilities.add(serecoVuln);
        return vulnerabilities;
    }

    private SecHubConfigurationModel createConfigWithSensitive() {
        return createConfiguration(true);
    }

    private SecHubConfigurationModel createConfigWithOutSensitive() {
        return createConfiguration(false);
    }

    private SecHubConfigurationModel createConfiguration(boolean withSensitiveHeaders) {
        String path = withSensitiveHeaders ? SECHUB_WEBSCAN_CONFIG_FILE_WITH_SENSITIVE_HEADERS : SECHUB_WEBSCAN_CONFIG_FILE_WITHOUT_SENSITIVE_HEADERS;

        String sechubConfigJson = TestFileReader.loadTextFile(path);
        SecHubConfigurationModel result = SecHubConfiguration.createFromJSON(sechubConfigJson);
        return result;
    }

    private void assertHeaderWasMasked(String headerName, List<SerecoVulnerability> maskedVulnerabilities) {
        for (SerecoVulnerability vulnerability : maskedVulnerabilities) {
            Map<String, String> requestHeaders = vulnerability.getWeb().getRequest().getHeaders();
            Map<String, String> responseHeaders = vulnerability.getWeb().getResponse().getHeaders();

            assertEquals(SensitiveDataMaskingService.SENSITIVE_DATA_MASK, requestHeaders.get(headerName));
            assertEquals(SensitiveDataMaskingService.SENSITIVE_DATA_MASK, responseHeaders.get(headerName));
        }
    }

    private void assertHeaderWasNotMasked(String headerName, String headerValue, List<SerecoVulnerability> maskedVulnerabilities) {
        for (SerecoVulnerability vulnerability : maskedVulnerabilities) {
            Map<String, String> requestHeaders = vulnerability.getWeb().getRequest().getHeaders();
            Map<String, String> responseHeaders = vulnerability.getWeb().getResponse().getHeaders();

            String actualRequestHeaderValue = requestHeaders.getOrDefault(headerName, "");
            if (actualRequestHeaderValue.equalsIgnoreCase(headerValue) == false) {
                fail("For header: '" + headerName + "' expected value: '" + headerValue + "', but got '" + actualRequestHeaderValue + "' instead!");
            }
            String actualResponseHeaderValue = responseHeaders.getOrDefault(headerName, "");
            if (actualResponseHeaderValue.equalsIgnoreCase(headerValue) == false) {
                fail("For header: '" + headerName + "' expected value: '" + headerValue + "', but got '" + actualResponseHeaderValue + "' instead!");
            }
        }
    }

}
