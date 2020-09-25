package com.daimler.sechub.client.java;

import static com.daimler.sechub.client.java.AssertJavaClientAPI.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.commons.model.TrafficLight;
public class SecHubClientTest {

    @Test
    public void client_reads_scan_code_green_no_findings() throws Exception {
        /* prepare */
        File file = new File("src/test/resources/scan_code_green_no_findings.json");

        /* execute */
        SecHubReport report = SecHubClient.importSecHubJsonReport(file);

        /* test */
        /* @formatter:off */
        assertReport(report).
            hasJobUUID("d47c1e28-9f76-4e43-a879-9af5184d505e").
            hasFindings(0).
            hasTrafficLight(TrafficLight.GREEN);
        /* @formatter:on */

    }

    @Test
    public void client_reads_scan_code_red_product_error() throws Exception {
        /* prepare */
        File file = new File("src/test/resources/scan_code_red_product_error.json");

        /* execute */
        SecHubReport report = SecHubClient.importSecHubJsonReport(file);

        /* test */
        /* @formatter:off */
        assertReport(report).
            hasJobUUID("94bcffcc-b995-4bb5-b3ad-9130cf743f35").
            hasFindings(1).
            hasTrafficLight(TrafficLight.RED).
            finding(0).
                hasId(1).
                hasSeverity(Severity.CRITICAL).
                hasNoHostnames().
                hasNoReferences().
                hasName("SecHub failure").
                hasDescription("Security product 'XYZ' failed, so cannot give a correct answer.");
        /* @formatter:on */
     
    }

   
    @Test
    public void client_reads_test_report_1() throws Exception {
        /* prepare */
        File file = new File("src/test/resources/test_sechub_report-1.json");

        /* execute */
        SecHubReport report = SecHubClient.importSecHubJsonReport(file);


        /* test */
        /* @formatter:off */
        assertReport(report).
            hasJobUUID("061234c8-40aa-4dcf-81f8-7bb8f723b780").
            hasFindings(277).
            hasTrafficLight(TrafficLight.YELLOW).
            finding(0).
                hasSeverity(Severity.MEDIUM).
                hasNoHostnames().
                hasNoReferences().
                hasName("Unsafe Object Binding").
                hasDescription(null);
        /* @formatter:on */
    }

    @Test
    public void client_reads_scan_code_yellow_with_findings() throws Exception {
        /* prepare */
        File file = new File("src/test/resources/scan_code_yellow_with_findings.json");

        /* execute */
        SecHubReport report = SecHubClient.importSecHubJsonReport(file);

        /* test */
        assertReport(report).
            hasJobUUID("6cf02ccf-da13-4dee-b529-0225ed9661bd").
            hasFindings(2).
            hasTrafficLight(TrafficLight.YELLOW).
            finding(0).
                hasId(1).
                hasSeverity(Severity.MEDIUM).
                hasNoHostnames().
                hasNoReferences().
                hasName("Absolute Path Traversal").
                hasDescription("").
                codeCall(0).
                    hasLine(28).
                    hasLocation("java/com/daimler/sechub/docgen/AsciidocGenerator.java").
                codeCall(1).
                    hasLine(33).
                    hasRelevantPart("args").
                    hasLocation("java/com/daimler/sechub/docgen/AsciidocGenerator.java").
                codeCall(2).
                    hasLine(33).
                    hasRelevantPart("path").
                    hasLocation("java/com/daimler/sechub/docgen/AsciidocGenerator.java").
                codeCall(3).
                hasLocation("java/com/daimler/sechub/docgen/AsciidocGenerator.java").
            finding(1).
                hasId(2).
                hasSeverity(Severity.LOW).
                hasName("Improper Exception Handling").
                hasDescription("").
                hasNoReferences().
                hasNoHostnames().
                codeCall(0).
                    hasLocation("java/com/daimler/sechub/docgen/usecase/UseCaseRestDocModelAsciiDocGenerator.java").
                    hasLine(112).
                    hasColumn(53).
                    hasSource("\t\tFile[] files = entry.copiedRestDocFolder.listFiles();").
                    hasRelevantPart("listFiles");
                
    }

    @Test
    public void client_reads_wrong_file() throws SecHubReportException {
        /* prepare */
        File file = new File("src/test/resources/no_sechub_report.json");

        /* execute + test */
        /* @formatter:off */
        assertThrows( 
                "The report is not a SecHub report and cannot be read. It should throw an exception.",
                SecHubReportException.class,
                () -> SecHubClient.importSecHubJsonReport(file));
        /* @formatter:on */
    }

}
