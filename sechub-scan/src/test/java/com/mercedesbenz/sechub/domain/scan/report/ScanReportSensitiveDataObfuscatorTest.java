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

    int sourceVisibleLength;

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

    @ValueSource(strings = { "a", "ab", "$*abcdefghiujklmnop", "" })
    @ParameterizedTest
    void obfuscate_full_secret_scan_source_when_default_source_visible_length_0(String text) {
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

    @ValueSource(ints = { -1, -3, -100 })
    @ParameterizedTest
    void do_not_obfuscate_secret_scan_sources_with_negative_source_visible_length(int sourceVisibleLength) {
        /* prepare */
        finding.setType(ScanType.SECRET_SCAN);
        codeCallStack.setSource("896%&98jerh345");
        ScanSecHubReport scanSecHubReport = createReport();
        obfuscatorToTest.sourceVisibleLength = sourceVisibleLength;

        /* execute */
        obfuscatorToTest.obfuscate(scanSecHubReport);

        /* test */
        List<SecHubFinding> findings = scanSecHubReport.getResult().getFindings();
        assertEquals(1, findings.size());
        SecHubFinding finding1 = findings.get(0);
        assertEquals("896%&98jerh345", finding1.getCode().getSource());
    }

    @Test
    void source_visible_length_3_shows_first_3_characters_from_secret_scan_source_and_postfix() {
        /* prepare */
        sourceVisibleLength = 3;
        finding.setType(ScanType.SECRET_SCAN);
        codeCallStack.setSource("896%&98jerh345");
        ScanSecHubReport scanSecHubReport = createReport();
        obfuscatorToTest.sourceVisibleLength = sourceVisibleLength;

        /* execute */
        obfuscatorToTest.obfuscate(scanSecHubReport);

        /* test */
        List<SecHubFinding> findings = scanSecHubReport.getResult().getFindings();
        assertEquals(1, findings.size());
        SecHubFinding finding1 = findings.get(0);
        assertEquals("896*****", finding1.getCode().getSource());
    }

    @ValueSource(ints = { 15, 80, 100 })
    @ParameterizedTest
    void show_all_characters_with_postfix_when_source_visible_length_greater_than_source_length(int sourceVisibleLength) {
        /* prepare */
        finding.setType(ScanType.SECRET_SCAN);
        codeCallStack.setSource("896%&98jerh345");
        ScanSecHubReport scanSecHubReport = createReport();
        obfuscatorToTest.sourceVisibleLength = sourceVisibleLength;

        /* execute */
        obfuscatorToTest.obfuscate(scanSecHubReport);

        /* test */
        List<SecHubFinding> findings = scanSecHubReport.getResult().getFindings();
        assertEquals(1, findings.size());
        SecHubFinding finding1 = findings.get(0);
        assertEquals("896%&98jerh345*****", finding1.getCode().getSource());
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class, names = { "SECRET_SCAN" }, mode = EnumSource.Mode.EXCLUDE)
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
    @ValueSource(ints = { 0, 1, 3, 100, -1, -3, -100 })
    void ignore_null_sources_for_obfuscation(int sourceVisibleLength) {
        /* prepare */
        finding.setType(ScanType.SECRET_SCAN);
        codeCallStack.setSource(null);
        ScanSecHubReport scanSecHubReport = createReport();
        obfuscatorToTest.sourceVisibleLength = sourceVisibleLength;

        /* execute + test */
        obfuscatorToTest.obfuscate(scanSecHubReport);
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