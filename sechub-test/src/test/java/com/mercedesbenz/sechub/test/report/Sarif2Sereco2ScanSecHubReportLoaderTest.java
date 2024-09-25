// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test.report;

import static com.mercedesbenz.sechub.test.report.ReportTestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;

/**
 * A special reporting test: Will test if existing SARIF reports are transformed
 * to wanted SecHub report format correctly (internally SARIF will be
 * transformed to SERECO and after this SERECO data will be transformed to final
 * SecHub report). <br>
 * <br>
 * This is exactly the same call mechanism as SecHub does internally, but
 * without false positive marking and using dedicated SARIF importer version V1
 * (but we have currently only this, so SecHub will use also this version only).
 * <br>
 * <br>
 * Our normal integrations tests do slow down our builds. Having SARIF already
 * tested by integration tests for SARIF code scans, we test here SARIF details
 * e.g. for web scanning - but much faster.
 *
 * @author Albert Tregnaghi
 *
 */
public class Sarif2Sereco2ScanSecHubReportLoaderTest {

    @Test
    void example1_owasp_zap_report_is_transformed_to_expected_sechub_report() throws Exception {
        /* prepare */
        String reportName = "example1_owasp_zap_report";

        String sarifJson = loadSarifReport(reportName);
        String expectedSecHubJson = loadExpectedSecHubReportOutputFile(reportName);

        String sechubJobUUID = "f5fdccc6-45d1-4b41-972c-08ff9ee0dddb";

        /* execute */
        String sechubJson = transformSarifToSecHubReportJSON(sarifJson, ProductIdentifier.PDS_WEBSCAN, sechubJobUUID);

        /* test */
        assertNotEquals(expectedSecHubJson, sechubJson);
        String prettyprinted = prettyPrintJson(sechubJson);
        assertEqualAndDumpToConsoleIfNot(expectedSecHubJson, prettyprinted);
    }

    @Test
    void example2_artifical_data_is_transformed_to_expected_sechub_report() throws Exception {
        /* prepare */
        String reportName = "example2_artificial_data";

        String sarifJson = loadSarifReport(reportName);
        String expectedSecHubJson = loadExpectedSecHubReportOutputFile(reportName);

        String sechubJobUUID = "f5fdccc6-45d1-4b42-972c-08ff9ee0dddb";

        /* execute */
        String sechubJson = transformSarifToSecHubReportJSON(sarifJson, ProductIdentifier.PDS_WEBSCAN, sechubJobUUID);

        /* test */
        assertNotEquals(expectedSecHubJson, sechubJson);
        String prettyprinted = prettyPrintJson(sechubJson);
        assertEqualAndDumpToConsoleIfNot(expectedSecHubJson, prettyprinted);
    }

    private String prettyPrintJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode read;
        try {
            read = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot pretty print", e);
        }
        return read.toPrettyString();
    }

    private void assertEqualAndDumpToConsoleIfNot(String expectedSecHubJson, String prettyprinted) {
        if (!expectedSecHubJson.equals(prettyprinted)) {
            System.out.println("---------- Unexpected (pretty printed) sechub report: -------------------");
            System.out.println(prettyprinted);
            System.out.println("-------------------------------------------------------------------------");
            assertEquals(expectedSecHubJson, prettyprinted, "Sechub report not as expected - please look into console for unexpected (pretty printed) output");
        }
    }

}
