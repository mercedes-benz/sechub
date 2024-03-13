package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import com.mercedesbenz.sechub.commons.model.*;

class ScanReportSensitiveDataObfuscatorTest {

    SecHubResult result;
    SecHubFinding finding;
    SecHubCodeCallStack codeCallStack;
    ScanReport report;
    ScanReportSensitiveDataObfuscator obfuscatorToTest;

    @BeforeEach
    void beforeEach() {
        obfuscatorToTest = new ScanReportSensitiveDataObfuscator();
        result = new SecHubResult();
        codeCallStack = new SecHubCodeCallStack();
        report = new ScanReport();
        finding = new SecHubFinding();

        finding.setName("finding");
        finding.setSeverity(Severity.CRITICAL);
    }

    @ValueSource(strings = { "a", "ab", "$*abcdefghiujklmnop" })
    @ParameterizedTest
    void obfuscate_full_secret_scan_secrets(String text) {
        /* prepare */
        finding.setType(ScanType.SECRET_SCAN);
        codeCallStack.setSource(text);
        ScanSecHubReport scanSecHubReport = createReport();

        /* execute */
        obfuscatorToTest.obfuscate(scanSecHubReport);

        /* test */
        List<SecHubFinding> findings = scanSecHubReport.getResult().getFindings();
        assertEquals(1, findings.size());
        SecHubFinding finding1 = findings.get(0);
        assertEquals("*****", finding1.getCode().getSource());
    }

    @Test
    void show_first_3_characters_from_secret_scan_secrets() {
        /* prepare */
        finding.setType(ScanType.SECRET_SCAN);
        codeCallStack.setSource("896%&98jerh345");
        ScanSecHubReport scanSecHubReport = createReport();
        obfuscatorToTest.sourceVisibleLength = 3;

        /* execute */
        obfuscatorToTest.obfuscate(scanSecHubReport);

        /* test */
        List<SecHubFinding> findings = scanSecHubReport.getResult().getFindings();
        assertEquals(1, findings.size());
        SecHubFinding finding1 = findings.get(0);
        assertEquals("896*****", finding1.getCode().getSource());
    }

    @Test
    void show_all_characters_secret_scan_secrets() {
        /* prepare */
        finding.setType(ScanType.SECRET_SCAN);
        codeCallStack.setSource("896%&98jerh345");
        ScanSecHubReport scanSecHubReport = createReport();
        obfuscatorToTest.sourceVisibleLength = 100;

        /* execute */
        obfuscatorToTest.obfuscate(scanSecHubReport);

        /* test */
        List<SecHubFinding> findings = scanSecHubReport.getResult().getFindings();
        assertEquals(1, findings.size());
        SecHubFinding finding1 = findings.get(0);
        assertEquals("896%&98jerh345*****", finding1.getCode().getSource());
    }

    @Test
    void non_obfuscate_characters_secret_scan_secrets() {
        /* prepare */
        finding.setType(ScanType.SECRET_SCAN);
        codeCallStack.setSource("896%&98jerh345");
        ScanSecHubReport scanSecHubReport = createReport();
        obfuscatorToTest.sourceVisibleLength = -3;

        /* execute */
        obfuscatorToTest.obfuscate(scanSecHubReport);

        /* test */
        List<SecHubFinding> findings = scanSecHubReport.getResult().getFindings();
        assertEquals(1, findings.size());
        SecHubFinding finding1 = findings.get(0);
        assertEquals("896%&98jerh345", finding1.getCode().getSource());
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class, names = { "CODE_SCAN", "WEB_SCAN", "INFRA_SCAN", "LICENSE_SCAN" })
    void do_not_obfuscate_other_scanTypes_sources_than_secret_scan(ScanType scanType) {
        /* prepare */
        finding.setType(scanType);
        codeCallStack.setSource("source_text");
        ScanSecHubReport scanSecHubReport = createReport();

        /* execute */
        obfuscatorToTest.obfuscate(scanSecHubReport);

        /* test */
        List<SecHubFinding> findings = scanSecHubReport.getResult().getFindings();
        assertEquals(1, findings.size());
        SecHubFinding finding1 = findings.get(0);
        assertEquals("source_text", finding1.getCode().getSource());
    }

    @ParameterizedTest
    @NullSource
    void ignore_null_sources_for_obfuscation(String text) {
        /* prepare */
        finding.setType(ScanType.SECRET_SCAN);
        codeCallStack.setSource(text);
        ScanSecHubReport scanSecHubReport = createReport();
        obfuscatorToTest.sourceVisibleLength = -3;

        /* execute + test */
        obfuscatorToTest.obfuscate(scanSecHubReport);
    }

    @ParameterizedTest
    @EmptySource
    void ignore_empty_sources_for_obfuscation(String text) {
        /* prepare */
        finding.setType(ScanType.SECRET_SCAN);
        codeCallStack.setSource(text);
        ScanSecHubReport scanSecHubReport = createReport();
        obfuscatorToTest.sourceVisibleLength = -3;

        /* execute */
        obfuscatorToTest.obfuscate(scanSecHubReport);

        /* test */
        List<SecHubFinding> findings = scanSecHubReport.getResult().getFindings();
        assertEquals(1, findings.size());
        SecHubFinding finding1 = findings.get(0);
        assertEquals("", finding1.getCode().getSource());
    }

    private ScanSecHubReport createReport() {
        finding.setCode(codeCallStack);
        result.getFindings().add(finding);

        report.setResult(result.toJSON());
        report.setTrafficLight(TrafficLight.GREEN);
        report.setResultType(ScanReportResultType.RESULT);

        return new ScanSecHubReport(report);
    }
}