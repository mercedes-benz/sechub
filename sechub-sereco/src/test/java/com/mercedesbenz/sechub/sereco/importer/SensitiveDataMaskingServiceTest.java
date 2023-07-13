// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubScanConfiguration;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.test.TestFileReader;

class SensitiveDataMaskingServiceTest {

    private SensitiveDataMaskingService serviceToTest = new SensitiveDataMaskingService();

    @Test
    void sensitive_headers_are_filtered_case_insensitive() throws IOException {
        /* prepare */
        String sechubConfigJson = TestFileReader.loadTextFile("src/test/resources/sechub-config-examples/sechub_webscan_config_with_sensitive_headers.json");
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(sechubConfigJson);

        String sarifReport = TestFileReader.loadTextFile("src/test/resources/sarif/sarif_2.1.0_owasp_zap_with_sensitive_headers.json");
        SarifV1JSONImporter importer = new SarifV1JSONImporter();
        SerecoMetaData data = importer.importResult(sarifReport, ScanType.WEB_SCAN);

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfig, data.getVulnerabilities());

        /* test */
        assertEquals(14, maskedVulnerabilities.size());
        assertHeaderWasMasked("authorization", maskedVulnerabilities);
        assertHeaderWasMasked("Api-Key", maskedVulnerabilities);
        assertHeaderWasNotMasked("x-file-size", "123456", maskedVulnerabilities);
    }

    @Test
    void non_sensitive_headers_are_not_changed() throws IOException {
        /* prepare */
        String sechubConfigJson = TestFileReader.loadTextFile("src/test/resources/sechub-config-examples/sechub_webscan_config_without_sensitive_headers.json");
        SecHubScanConfiguration scanConfig = SecHubScanConfiguration.createFromJSON(sechubConfigJson);

        String sarifReport = TestFileReader.loadTextFile("src/test/resources/sarif/sarif_2.1.0_owasp_zap_without_sensitive_headers.json");
        SarifV1JSONImporter importer = new SarifV1JSONImporter();
        SerecoMetaData data = importer.importResult(sarifReport, ScanType.WEB_SCAN);

        /* execute */
        List<SerecoVulnerability> maskedVulnerabilities = serviceToTest.maskSensitiveData(scanConfig, data.getVulnerabilities());

        /* test */
        assertEquals(14, maskedVulnerabilities.size());
        assertHeaderWasNotMasked("x-file-size", "123456", maskedVulnerabilities);
        assertHeaderWasNotMasked("custom-header", "non-secret-value", maskedVulnerabilities);
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
                if (!actualHeaderValue.equalsIgnoreCase(headerValue)) {
                    fail("For header: '" + headerName + "' expected value: '" + headerValue + "', but got '" + actualHeaderValue + "' instead!");
                }
            }
            if (responseHeaders.containsKey(headerName)) {
                String actualHeaderValue = responseHeaders.get(headerName);
                if (!actualHeaderValue.equalsIgnoreCase(headerValue)) {
                    fail("For header: '" + headerName + "' expected value: '" + headerValue + "', but got '" + actualHeaderValue + "' instead!");
                }
            }
        }
    }

}
