package com.daimler.sechub.client.java;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.client.java.core.SecHubReportReadingException;
import com.daimler.sechub.client.java.report.SecHubCodeCallStack;
import com.daimler.sechub.client.java.report.SecHubFinding;
import com.daimler.sechub.client.java.report.SecHubReport;
import com.daimler.sechub.client.java.report.SecHubResult;
import com.daimler.sechub.client.java.report.Severity;
import com.daimler.sechub.client.java.report.TrafficLight;




public class SecHubClientTest {

    @Test
    public void client_reads_scan_code_green_no_findings() throws Exception {
        /* prepare */
        File file = new File("src/test/resources/scan_code_green_no_findings.json");

        /* execute */
        SecHubReport report = SecHubClient.importSecHubJsonReport(file);
        SecHubResult result = report.getResult();

        /* test */
        assertNotNull("Report may not be null",report);
        assertEquals(report.getJobUUID(), UUID.fromString("d47c1e28-9f76-4e43-a879-9af5184d505e"));
        assertEquals(report.getTrafficLight(), TrafficLight.GREEN);
        assertNotNull("Report result may not be null",result);
        assertEquals(result.getCount(), 0);
        assertTrue(result.getFindings().isEmpty());

    }

    @Test
    public void client_reads_scan_code_red_checkmarks_error() throws Exception {
        /* prepare */
        File file = new File("src/test/resources/scan_code_red_checkmarks_error.json");

        SecHubReport report;
        /* execute */
        report = SecHubClient.importSecHubJsonReport(file);

        SecHubResult result = report.getResult();
        List<SecHubFinding> findings = result.getFindings();

        /* test */
        assertNotNull("Report may not be null",report);
        assertEquals(report.getJobUUID(), UUID.fromString("94bcffcc-b995-4bb5-b3ad-9130cf743f35"));
        assertEquals(report.getTrafficLight(), TrafficLight.RED);
        assertNotNull("Report result may not be null",result);
        assertEquals(result.getCount(), 0);

        SecHubFinding firstFinding = findings.get(0);

        assertEquals(firstFinding.getId(), 1);
        assertEquals(firstFinding.getDescription(), "Security product 'CHECKMARX' failed, so cannot give a correct answer.");
        assertTrue(firstFinding.getHostnames().isEmpty());
        assertEquals(firstFinding.getName(), "SecHub failure");
        assertTrue(firstFinding.getReferences().isEmpty());
        assertEquals(firstFinding.getSeverity(), Severity.CRITICAL);
    }

    @Test
    public void client_reads_scan_code_yellow_with_findings() throws Exception {
        /* prepare */
        File file = new File("src/test/resources/scan_code_yellow_with_findings.json");

        /* execute */
        SecHubReport report = SecHubClient.importSecHubJsonReport(file);
        SecHubResult result = report.getResult();
        List<SecHubFinding> findings = result.getFindings();

        /* test */
        assertNotNull("Report may not be null",report);
        assertEquals(report.getJobUUID(), UUID.fromString("6cf02ccf-da13-4dee-b529-0225ed9661bd"));
        assertEquals(report.getTrafficLight(), TrafficLight.YELLOW);
        assertNotNull("Report result may not be null",result);
        assertEquals(result.getCount(), 2);

        SecHubFinding firstFinding = findings.get(0);

        assertEquals(firstFinding.getId(), 1);
        assertEquals(firstFinding.getDescription(), "");
        assertTrue(firstFinding.getHostnames().isEmpty());
        assertEquals(firstFinding.getName(), "Absolute Path Traversal");
        assertTrue(firstFinding.getReferences().isEmpty());
        assertEquals(firstFinding.getSeverity(), Severity.MEDIUM);

        SecHubCodeCallStack code = firstFinding.getCode();
        assertNotNull(code);
        assertNotNull(code.getCalls().getCalls().getCalls().getCalls());
        assertNull(code.getCalls().getCalls().getCalls().getCalls().getCalls());

        SecHubFinding secondFinding = findings.get(1);

        assertEquals(secondFinding.getId(), 2);
        assertEquals(secondFinding.getDescription(), "");
        assertTrue(secondFinding.getHostnames().isEmpty());
        assertEquals(secondFinding.getName(), "Improper Exception Handling");
        assertTrue(secondFinding.getReferences().isEmpty());
        assertEquals(secondFinding.getSeverity(), Severity.LOW);

        SecHubCodeCallStack code2 = secondFinding.getCode();
        assertEquals(code2.getLocation(), "java/com/daimler/sechub/docgen/usecase/UseCaseRestDocModelAsciiDocGenerator.java");
        assertEquals(code2.getLine(), new Integer(112));
        assertEquals(code2.getColumn(), new Integer(53));
        assertEquals(code2.getSource(), "\t\tFile[] files = entry.copiedRestDocFolder.listFiles();");
        assertEquals(code2.getRelevantPart(), "listFiles");
        assertNull(code2.getCalls());
    }

    @Test
    public void client_reads_wrong_file() throws SecHubReportReadingException {
        /* prepare */
        File file = new File("src/test/resources/no_sechub_report.json");

        /* execute + test */
        /* @formatter:off */
        assertThrows( 
                "The report is not a SecHub report and cannot be read. It should throw an exception.",
                SecHubReportReadingException.class,
                () -> SecHubClient.importSecHubJsonReport(file));
        /* @formatter:on */
    }

}
