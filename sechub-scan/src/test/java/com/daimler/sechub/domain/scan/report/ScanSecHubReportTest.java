package com.daimler.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.daimler.sechub.commons.model.SecHubFinding;
import com.daimler.sechub.commons.model.SecHubMessage;
import com.daimler.sechub.commons.model.SecHubMessageType;
import com.daimler.sechub.commons.model.SecHubReportModel;
import com.daimler.sechub.commons.model.SecHubResult;
import com.daimler.sechub.commons.model.SecHubStatus;
import com.daimler.sechub.commons.model.Severity;
import com.daimler.sechub.commons.model.TrafficLight;

class ScanSecHubReportTest {

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
        ScanSecHubReport resultToTest = new ScanSecHubReport(report);

        /* test */
        assertEquals(2, resultToTest.getResult().getCount());
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
        ScanSecHubReport createdResult = new ScanSecHubReport(report);
        // no we also check if the JSON deserialization /serialization works as expected
        String json = createdResult.toJSON();
        ScanSecHubReport resultToTest = ScanSecHubReport.fromJSONString(json);

        /* test */
        assertEquals(TrafficLight.GREEN, resultToTest.getTrafficLight());
    }
    
    @Test
    void scanreport_result_by_simple_result_does_not_recalculates_traffic_light_but_uses_report_traffic_light() {
        
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
        ScanSecHubReport createdResult = new ScanSecHubReport(report);
        // no we also check if the JSON deserialization /serialization works as expected
        String json = createdResult.toJSON();
        ScanSecHubReport resultToTest = ScanSecHubReport.fromJSONString(json);
        
        /* test */
        assertEquals(TrafficLight.GREEN, resultToTest.getTrafficLight());
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
        ScanSecHubReport createdResult = new ScanSecHubReport(report);
        // no we also check if the JSON deserialization /serialization works as expected
        String json = createdResult.toJSON();
        ScanSecHubReport resultToTest = ScanSecHubReport.fromJSONString(json);

        /* test */
        assertEquals(SecHubStatus.OK, resultToTest.getStatus()); // no status available from simple result, expecting OK
        assertEquals(TrafficLight.YELLOW, resultToTest.getTrafficLight());
        assertEquals(0, resultToTest.getMessages().size());
        assertEquals(1, resultToTest.getResult().getFindings().size());
        assertEquals(1, resultToTest.getResult().getCount());
    }

    @Test
    void scanreport_result_with_report_containing_sechub_report_model_init_and_son_parts_work() {

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
        ScanSecHubReport createdResult = new ScanSecHubReport(report);
        // no we also check if the JSON deserialization /serialization works as expected
        String json = createdResult.toJSON();
        ScanSecHubReport resultToTest = ScanSecHubReport.fromJSONString(json);

        /* test */
        assertEquals(SecHubStatus.FAILED, resultToTest.getStatus());
        assertEquals(null, resultToTest.getTrafficLight()); // traffic light was not set at all
        assertEquals(1, resultToTest.getMessages().size());
        SecHubMessage message1 = resultToTest.getMessages().iterator().next();
        assertEquals("Testwarning", message1.getText());
        assertEquals(SecHubMessageType.WARNING, message1.getType());
        assertEquals(1, resultToTest.getResult().getFindings().size());
        assertEquals(1, resultToTest.getResult().getCount());
    }

}
