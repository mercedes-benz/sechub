// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubReportModel;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.SecHubStatus;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.domain.scan.ScanDomainTestFileSupport;

class ScanSecHubReportTest {

    @Test
    void a_sechub_report_without_status_messages_or_report_version_can_be_deserialized() {

        /* prepare */
        String fileName = "sechub-report-example2-simple-finding-no-status-no-reportVersion.json";

        /* execute */
        ScanSecHubReport report = deserializeReportFile(fileName);

        /* test */
        assertNull(report.getStatus(), "Status must be null");
        assertNull(report.getReportVersion(), "Report version must be null");
        assertEquals(0, report.getMessages().size(), "No messages");

    }

    @Test
    void a_sechub_report_without_status_messages_or_report_version_can_be_serialized() {

        /* prepare */
        String fileName = "sechub-report-example2-simple-finding-no-status-no-reportVersion.json";
        ScanSecHubReport report = deserializeReportFile(fileName);

        /* check preconditions */
        assertNull(report.getStatus(), "Status must be null");
        assertNull(report.getReportVersion(), "Report version must be null");
        assertEquals(0, report.getMessages().size(), "No messages");

        /* execute */
        String json = report.toJSON();

        /* test */
        assertTrue(json.contains("messages")); // messages is available but empty
        assertFalse(json.contains("status")); // status is null, so not in JSON
        assertFalse(json.contains("reportVersion")); // reportVersion is null, so not in JSON

    }

    private ScanSecHubReport deserializeReportFile(String fileName) {
        File file = new File("./src/test/resources/sechub_result/" + fileName);
        ScanSecHubReport report = ScanSecHubReport.fromJSONString(ScanDomainTestFileSupport.loadTextFile(file, "\n"));
        return report;
    }

    @Test
    void scanreport_result_recaclulates_count() {
        /* prepare */
        SecHubResult result = new SecHubResult();
        SecHubFinding finding1 = new SecHubFinding();
        finding1.setName("finding1");

        SecHubFinding finding2 = new SecHubFinding();
        finding2.setName("finding2");

        List<SecHubFinding> findings = result.getFindings();
        findings.add(finding1);
        findings.add(finding2);

        result.setCount(1000);

        ScanReport report = new ScanReport();
        report.setResult(result.toJSON());

        /* execute */
        ScanSecHubReport reportToTest = new ScanSecHubReport(report);

        /* test */
        assertEquals(2, reportToTest.getResult().getCount());
    }

    @Test
    void scanreport_result_by_report_model_does_not_recalculates_traffic_light_but_uses_report_traffic_light() {

        /* prepare */
        SecHubResult result = new SecHubResult();
        SecHubFinding finding = new SecHubFinding();
        finding.setName("finding1");
        finding.setSeverity(Severity.CRITICAL);
        result.getFindings().add(finding);

        ScanReport report = new ScanReport();
        report.setResult(result.toJSON());
        report.setTrafficLight(TrafficLight.GREEN);
        report.setResultType(ScanReportResultType.MODEL);

        /* execute */
        ScanSecHubReport createdReport = new ScanSecHubReport(report);
        // now we also check if the JSON deserialization /serialization works as
        // expected
        String json = createdReport.toJSON();
        ScanSecHubReport reportToTest = ScanSecHubReport.fromJSONString(json);

        /* test */
        assertEquals(TrafficLight.GREEN, reportToTest.getTrafficLight());
    }

    @Test
    void report_by_model_sets_jobUUID_from_scanreport_when_not_inside_model() {

        /* prepare */
        UUID uuid = UUID.randomUUID();

        ScanReport report = mock(ScanReport.class);
        when(report.getResultType()).thenReturn(ScanReportResultType.MODEL);
        when(report.getSecHubJobUUID()).thenReturn(uuid);

        SecHubReportModel model = new SecHubReportModel();

        String jsonResult = model.toJSON();
        when(report.getResult()).thenReturn(jsonResult);

        /* execute */
        ScanSecHubReport createdReport = new ScanSecHubReport(report);

        /* test */
        assertEquals(uuid, createdReport.getJobUUID());
    }

    @Test
    void report_by_model_has_jobUUID_from_model_when_there_not_null() {

        /* prepare */
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        ScanReport report = mock(ScanReport.class);
        when(report.getResultType()).thenReturn(ScanReportResultType.MODEL);
        when(report.getSecHubJobUUID()).thenReturn(uuid1);

        SecHubReportModel model = new SecHubReportModel();
        model.setJobUUID(uuid2);

        String jsonResult = model.toJSON();
        when(report.getResult()).thenReturn(jsonResult);

        /* execute */
        ScanSecHubReport createdReport = new ScanSecHubReport(report);

        /* test */
        assertEquals(uuid2, createdReport.getJobUUID());
    }

    @Test
    void report_by_model_sets_version_to_version_from_model() {

        /* prepare */
        SecHubReportModel model = new SecHubReportModel();
        model.setReportVersion("42.0");

        ScanReport report = new ScanReport();
        report.setResult(model.toJSON());
        report.setResultType(ScanReportResultType.MODEL);

        /* execute */
        ScanSecHubReport createdReport = new ScanSecHubReport(report);

        /* test */
        assertEquals("42.0", createdReport.getReportVersion());
    }

    @Test
    void scanreport_result_by_reesult_does_NOT_set_version() {

        /* prepare */
        SecHubResult result = new SecHubResult();

        ScanReport report = new ScanReport();
        report.setResult(result.toJSON());
        report.setResultType(ScanReportResultType.RESULT);

        /* execute */
        ScanSecHubReport createdReport = new ScanSecHubReport(report);

        /* test */
        assertEquals(null, createdReport.getReportVersion());
    }

    @Test
    void scanreport_result_by_simple_result_does_not_recalculate_traffic_light_but_uses_report_traffic_light() {

        /* prepare */
        SecHubResult result = new SecHubResult();
        SecHubFinding finding = new SecHubFinding();
        finding.setName("finding1");
        finding.setSeverity(Severity.CRITICAL);
        result.getFindings().add(finding);

        ScanReport report = new ScanReport();
        report.setResult(result.toJSON());
        report.setTrafficLight(TrafficLight.GREEN);
        report.setResultType(ScanReportResultType.RESULT);

        /* execute */
        ScanSecHubReport createdReport = new ScanSecHubReport(report);
        // now we also check if the JSON deserialization /serialization works as
        // expected
        String json = createdReport.toJSON();
        ScanSecHubReport reportToTest = ScanSecHubReport.fromJSONString(json);

        /* test */
        assertEquals(TrafficLight.GREEN, reportToTest.getTrafficLight());
    }

    @Test
    void scanreport_result_with_report_containing_sechub_result_init_and_json_parts_work() {

        /* prepare */
        SecHubResult result = new SecHubResult();
        SecHubFinding finding = new SecHubFinding();
        finding.setName("finding1");
        result.getFindings().add(finding);

        ScanReport report = new ScanReport();
        report.setResult(result.toJSON());
        report.setTrafficLight(TrafficLight.YELLOW);
        report.setResultType(ScanReportResultType.RESULT);

        /* execute */
        ScanSecHubReport createdReport = new ScanSecHubReport(report);
        // now we also check if the JSON deserialization /serialization works as
        // expected
        String json = createdReport.toJSON();
        ScanSecHubReport reportToTest = ScanSecHubReport.fromJSONString(json);

        /* test */
        assertEquals(SecHubStatus.SUCCESS, reportToTest.getStatus()); // no status available from simple result, expecting OK
        assertEquals(TrafficLight.YELLOW, reportToTest.getTrafficLight());
        assertEquals(0, reportToTest.getMessages().size());
        assertEquals(1, reportToTest.getResult().getFindings().size());
        assertEquals(1, reportToTest.getResult().getCount());
    }

    @Test
    void scanreport_result_with_report_containing_sechub_report_model_init_and_json_parts_work() {

        /* prepare */
        SecHubReportModel reportModel = new SecHubReportModel();
        SecHubFinding finding = new SecHubFinding();
        finding.setName("finding1");
        reportModel.getResult().getFindings().add(finding);
        reportModel.setStatus(SecHubStatus.FAILED);
        reportModel.getMessages().add(new SecHubMessage(SecHubMessageType.WARNING, "Testwarning"));

        ScanReport report = new ScanReport();
        report.setResult(reportModel.toJSON());
        report.setResultType(ScanReportResultType.MODEL);

        /* execute */
        ScanSecHubReport createdReport = new ScanSecHubReport(report);
        // now we also check if the JSON deserialization /serialization works as
        // expected
        String json = createdReport.toJSON();
        ScanSecHubReport reportToTest = ScanSecHubReport.fromJSONString(json);

        /* test */
        assertEquals(SecHubStatus.FAILED, reportToTest.getStatus());
        assertEquals(null, reportToTest.getTrafficLight()); // traffic light was not set at all
        assertEquals(1, reportToTest.getMessages().size());
        SecHubMessage message1 = reportToTest.getMessages().iterator().next();
        assertEquals("Testwarning", message1.getText());
        assertEquals(SecHubMessageType.WARNING, message1.getType());
        assertEquals(1, reportToTest.getResult().getFindings().size());
        assertEquals(1, reportToTest.getResult().getCount());
    }

}
