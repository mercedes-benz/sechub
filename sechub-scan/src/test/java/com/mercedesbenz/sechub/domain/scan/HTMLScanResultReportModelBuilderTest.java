// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.domain.scan.report.ScanSecHubReport;

class HTMLScanResultReportModelBuilderTest {

    /*
     * we use own variables here and not the constants from class to test, to
     * separate test from impl...
     */
    private static final String SHOW_LIGHT = "opacity: 1.0";
    private static final String HIDE_LIGHT = "opacity: 0.25";

    private HTMLScanResultReportModelBuilder builderToTest;
    private ScanSecHubReport scanSecHubReport;
    private SecHubResultTrafficLightFilter trafficLightFilter;
    private SecHubResult result;
    private List<SecHubFinding> greenList;
    private List<SecHubFinding> redList;
    private List<SecHubFinding> yellowList;
    private ScanTypeCount scanTypeCount;

    @BeforeEach
    void beforeEach() throws Exception {
        builderToTest = new HTMLScanResultReportModelBuilder();

        trafficLightFilter = mock(SecHubResultTrafficLightFilter.class);
        Resource cssResource = mock(Resource.class);
        when(cssResource.getInputStream()).thenReturn(new ByteArrayInputStream("csscontentfromstream".getBytes()));

        builderToTest.cssResource = cssResource;
        builderToTest.trafficLightFilter = trafficLightFilter;

        result = mock(SecHubResult.class);

        scanSecHubReport = mock(ScanSecHubReport.class);
        when(scanSecHubReport.getResult()).thenReturn(result);

        greenList = new ArrayList<>();
        redList = new ArrayList<>();
        yellowList = new ArrayList<>();

        when(trafficLightFilter.filterFindingsFor(result, TrafficLight.RED)).thenReturn(redList);
        when(trafficLightFilter.filterFindingsFor(result, TrafficLight.YELLOW)).thenReturn(yellowList);
        when(trafficLightFilter.filterFindingsFor(result, TrafficLight.GREEN)).thenReturn(greenList);

        scanTypeCount = ScanTypeCount.of(ScanType.CODE_SCAN);
    }

    @Test void metaData_set_as_optional_not_present_when_configuration_has_metadata_optional_null() {
        /* prepare */
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.YELLOW); // traffic light necessary to avoid illegal state exception
        when(scanSecHubReport.getMetaData()).thenReturn(Optional.ofNullable(null));

        /* execute */
        Map<String, Object> map = builderToTest.build(scanSecHubReport);

        /* test */
        @SuppressWarnings("unchecked") Optional<SecHubReportMetaData> metaData = (Optional<SecHubReportMetaData>) map.get("metaData");
        assertNotNull(metaData);
        assertFalse(metaData.isPresent());
    }

    @Test void metaData_set_as_optional_not_present_when_configuration_has_metadata_optional_defined() {
        /* prepare */
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.YELLOW); // traffic light necessary to avoid illegal state exception
        SecHubReportMetaData reportMetaData = mock(SecHubReportMetaData.class);
        when(scanSecHubReport.getMetaData()).thenReturn(Optional.ofNullable(reportMetaData));

        /* execute */
        Map<String, Object> map = builderToTest.build(scanSecHubReport);

        /* test */
        @SuppressWarnings("unchecked") Optional<SecHubReportMetaData> metaData = (Optional<SecHubReportMetaData>) map.get("metaData");
        assertNotNull(metaData);
        assertTrue(metaData.isPresent());
    }

    @Test
    void all_parameters_build_webdesignmode_false() {
        /* prepare */
        UUID uuid = UUID.randomUUID();

        when(scanSecHubReport.getJobUUID()).thenReturn(uuid);
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.RED);

        builderToTest.webDesignMode = false;
        builderToTest.embeddedCSS = "embeddedCssContent";

        /* execute */
        Map<String, Object> map = builderToTest.build(scanSecHubReport);

        /* test */
        assertSame(result, map.get("result"));
        assertSame(greenList, map.get("greenList"));
        assertSame(redList, map.get("redList"));
        assertSame(yellowList, map.get("yellowList"));
        assertEquals(false, map.get("isWebDesignMode"));
        assertNull(map.get("${includedCSSRef}"));

        assertEquals("RED", map.get("trafficlight"));
        assertEquals(uuid.toString(), map.get("jobuuid"));
        assertEquals(SHOW_LIGHT, map.get("styleRed"));
        assertEquals(HIDE_LIGHT, map.get("styleYellow"));
        assertEquals(HIDE_LIGHT, map.get("styleGreen"));
    }

    @Test
    void all_parameters_build_webdesignmode_true() throws Exception {
        /* prepare */
        UUID uuid = UUID.randomUUID();

        when(scanSecHubReport.getJobUUID()).thenReturn(uuid);
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.YELLOW);
        builderToTest.webDesignMode = true;
        builderToTest.embeddedCSS = "embeddedCssContent";
        Resource cssResource = mock(Resource.class);
        File expectedFile = ScanDomainTestFileSupport.getTestfileSupport()
                .createFileFromRoot("sechub-scan/src/main/resources/templates/report/html/scanresult.css");
        when(cssResource.getFile()).thenReturn(expectedFile);
        builderToTest.cssResource = cssResource;

        /* execute */
        Map<String, Object> map = builderToTest.build(scanSecHubReport);

        /* test */
        assertSame(result, map.get("result"));
        assertSame(greenList, map.get("greenList"));
        assertSame(redList, map.get("redList"));
        assertSame(yellowList, map.get("yellowList"));
        assertEquals(true, map.get("isWebDesignMode"));

        // check css ref for webdesign mode
        assertNotNull(map.get("includedCSSRef"));
        String path = (String) map.get("includedCSSRef");
        File foundFile = new File(path);

        assertEquals(expectedFile.getCanonicalPath(), foundFile.getCanonicalPath());

        assertEquals("YELLOW", map.get("trafficlight"));
        assertEquals(uuid.toString(), map.get("jobuuid"));
        assertEquals(HIDE_LIGHT, map.get("styleRed"));
        assertEquals(SHOW_LIGHT, map.get("styleYellow"));
        assertEquals(HIDE_LIGHT, map.get("styleGreen"));
    }

    @Test void trafficlight_red_set_display_block__others_are_none() {
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.RED);

        Map<String, Object> map = builderToTest.build(scanSecHubReport);
        assertEquals(SHOW_LIGHT, map.get("styleRed"));
        assertEquals(HIDE_LIGHT, map.get("styleYellow"));
        assertEquals(HIDE_LIGHT, map.get("styleGreen"));
    }

    @Test public void trafficlight_yellow_set_display_block__others_are_none() {
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.YELLOW);

        Map<String, Object> map = builderToTest.build(scanSecHubReport);
        assertEquals(HIDE_LIGHT, map.get("styleRed"));
        assertEquals(SHOW_LIGHT, map.get("styleYellow"));
        assertEquals(HIDE_LIGHT, map.get("styleGreen"));
    }

    @Test void trafficlight_green_set_display_block__others_are_none() {
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.GREEN);

        Map<String, Object> map = builderToTest.build(scanSecHubReport);
        assertEquals(HIDE_LIGHT, map.get("styleRed"));
        assertEquals(HIDE_LIGHT, map.get("styleYellow"));
        assertEquals(SHOW_LIGHT, map.get("styleGreen"));
    }

    @Test
    void code_scan_entries_set_and_right_amount_of_call_stacks_populated() {

        /* prepare */
        SecHubFinding finding = mock(SecHubFinding.class);
        SecHubCodeCallStack code1 = mock(SecHubCodeCallStack.class);
        SecHubCodeCallStack subCode = mock(SecHubCodeCallStack.class);

        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.RED);
        when(scanSecHubReport.getResult()).thenReturn(result);
        when(result.getFindings()).thenReturn(Arrays.asList(finding));
        when(finding.getCode()).thenReturn(code1);
        when(finding.getType()).thenReturn(ScanType.CODE_SCAN);
        when(finding.getSeverity()).thenReturn(Severity.HIGH);
        when(finding.getName()).thenReturn("some_vulnerability_name");
        when(code1.getCalls()).thenReturn(subCode);

        /* execute */
        Map<String, Object> buildResult = builderToTest.build(scanSecHubReport);

        /* test */
        assertNotNull(buildResult.get("codeScanEntries"));

        assertTrue(buildResult.get("codeScanEntries") instanceof Map<?, ?>);

        @SuppressWarnings("unchecked")
        Map<Integer, List<HTMLScanResultCodeScanEntry>> codeScanEntries = (Map<Integer, List<HTMLScanResultCodeScanEntry>>) buildResult.get("codeScanEntries");
        assertNotNull(codeScanEntries.get(0));

        List<HTMLScanResultCodeScanEntry> scanEntriesList = codeScanEntries.get(0);
        assertTrue(scanEntriesList.size() == 2);
    }

    @Test void code_scan_support_set_and_not_null() {
        /* prepare */
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.RED);

        /* execute */
        Map<String, Object> map = builderToTest.build(scanSecHubReport);

        /* test */
        assertNotNull(map.get("codeScanSupport"));
        assertTrue(map.get("codeScanSupport") instanceof HtmlCodeScanDescriptionSupport);
    }

    @Test
    void when_severity_is_critical_then_highSeverityCount_should_be_incremented() {
        /* execute */
        builderToTest.incrementScanCount(Severity.CRITICAL, scanTypeCount);

        /* test */
        assertEquals(1, scanTypeCount.getHighSeverityCount());
    }

    @Test
    void when_severity_is_high_then_highSeverityCount_should_be_incremented() {
        /* execute */
        builderToTest.incrementScanCount(Severity.HIGH, scanTypeCount);

        /* test */
        assertEquals(1, scanTypeCount.getHighSeverityCount());
    }

    @Test
    void when_severity_is_medium_then_mediumSeverityCount_should_be_incremented() {
        /* execute */
        builderToTest.incrementScanCount(Severity.MEDIUM, scanTypeCount);

        /* test */
        assertEquals(1, scanTypeCount.getMediumSeverityCount());
    }

    @Test
    void when_severity_is_low_then_lowSeverityCount_should_be_incremented() {
        /* execute */
        builderToTest.incrementScanCount(Severity.LOW, scanTypeCount);

        /* test */
        assertEquals(1, scanTypeCount.getLowSeverityCount());
    }

    @Test
    void when_severity_is_info_then_lowSeverityCount_should_be_incremented() {
        /* execute */
        builderToTest.incrementScanCount(Severity.INFO, scanTypeCount);

        /* test */
        assertEquals(1, scanTypeCount.getLowSeverityCount());
    }

    @Test
    void when_severity_is_unclassified_then_lowSeverityCount_should_be_incremented() {
        /* execute */
        builderToTest.incrementScanCount(Severity.UNCLASSIFIED, scanTypeCount);

        /* test */
        assertEquals(1, scanTypeCount.getLowSeverityCount());
    }

    @Test
    void when_findings_list_is_empty_then_prepareScanTypesForModel_returns_empty_set() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();

        /* execute */
        Set<ScanTypeCount> scanTypeCountSet = builderToTest.createScanTypeCountSet(findings);

        /* test */
        assertTrue(scanTypeCountSet.isEmpty());
    }

    @Test
    void when_findings_list_contains_multiple_CODE_SCAN_findings_only_then_prepareScanTypesForModel_returns_set_with_one_appropriate_element_only() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();
        SecHubFinding finding = new SecHubFinding();
        finding.setId(0);
        finding.setType(ScanType.CODE_SCAN);
        finding.setSeverity(Severity.LOW);
        findings.add(finding);
        finding = new SecHubFinding();
        finding.setId(1);
        finding.setType(ScanType.CODE_SCAN);
        finding.setSeverity(Severity.MEDIUM);
        findings.add(finding);
        finding = new SecHubFinding();
        finding.setId(2);
        finding.setType(ScanType.CODE_SCAN);
        finding.setSeverity(Severity.HIGH);
        findings.add(finding);

        /* execute */
        Set<ScanTypeCount> scanTypeCountSet = builderToTest.createScanTypeCountSet(findings);
        Iterator<ScanTypeCount> iterator = scanTypeCountSet.iterator();
        ScanTypeCount scanTypeCount = iterator.next();

        /* test */
        assertTrue(scanTypeCountSet.size() == 1);
        assertEquals(ScanType.CODE_SCAN, scanTypeCount.getScanType());
        assertEquals(1, scanTypeCount.getLowSeverityCount());
        assertEquals(1, scanTypeCount.getMediumSeverityCount());
        assertEquals(1, scanTypeCount.getHighSeverityCount());
    }

    @Test
    void when_findings_list_contains_multiple_findings_of_differernt_ScanType_then_prepareScanTypesForModel_returns_appropriate_set() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();
        SecHubFinding finding = new SecHubFinding();
        finding.setId(0);
        finding.setType(ScanType.CODE_SCAN);
        finding.setSeverity(Severity.LOW);
        findings.add(finding);
        finding = new SecHubFinding();
        finding.setId(1);
        finding.setType(ScanType.INFRA_SCAN);
        finding.setSeverity(Severity.MEDIUM);
        findings.add(finding);
        finding = new SecHubFinding();
        finding.setId(2);
        finding.setType(ScanType.SECRET_SCAN);
        finding.setSeverity(Severity.HIGH);
        findings.add(finding);

        /* execute */
        Set<ScanTypeCount> scanTypeCountSet = builderToTest.createScanTypeCountSet(findings);
        Iterator<ScanTypeCount> iterator = scanTypeCountSet.iterator();

        /* execute + test */
        assertTrue(scanTypeCountSet.size() == 3);

        while (iterator.hasNext()) {
            ScanTypeCount scanTypeCount = iterator.next();
            switch (scanTypeCount.getScanType()) {
            case CODE_SCAN -> assertEquals(1, scanTypeCount.getLowSeverityCount());
            case INFRA_SCAN -> assertEquals(1, scanTypeCount.getMediumSeverityCount());
            case SECRET_SCAN -> assertEquals(1, scanTypeCount.getHighSeverityCount());
            }
        }
    }

    @Test
    void when_findings_list_is_empty_then_filterFindingsForWebScan_must_return_empty_map() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();
        List<Severity> severities = List.of(Severity.HIGH);

        /* execute */
        Map<String, List<SecHubFinding>> groupedAndSortedFindingsByName = builderToTest.createWebScanDataForSeverityGroupedAndSortedByName(findings, severities);

        /* test */
        assertTrue(groupedAndSortedFindingsByName.isEmpty());
    }

    @Test
    void when_findings_list_contains_3_WEB_SCAN_HIGH_findings_then_filterFindingsForWebScan_must_return_appropriate_map() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();
        SecHubFinding finding1 = new SecHubFinding();
        finding1.setId(0);
        finding1.setType(ScanType.WEB_SCAN);
        finding1.setSeverity(Severity.HIGH);
        finding1.setName("Cross Site Scripting (Reflected)");
        findings.add(finding1);
        
        SecHubFinding finding2 = new SecHubFinding();
        finding2.setId(1);
        finding2.setType(ScanType.WEB_SCAN);
        finding2.setSeverity(Severity.HIGH);
        finding2.setName("Cross Site Scripting (Reflected)");
        findings.add(finding2);
        
        SecHubFinding finding3 = new SecHubFinding();
        finding3.setId(2);
        finding3.setType(ScanType.WEB_SCAN);
        finding3.setSeverity(Severity.HIGH);
        finding3.setName("Cross Site Scripting (Reflected)");
        findings.add(finding3);

        List<Severity> severities = List.of(Severity.HIGH);

        /* execute */
        Map<String, List<SecHubFinding>> groupedAndSortedFindingsByName = builderToTest.createWebScanDataForSeverityGroupedAndSortedByName(findings, severities);
        List<SecHubFinding> findingList = groupedAndSortedFindingsByName.get("Cross Site Scripting (Reflected)");

        /* test */
        assertEquals(1, groupedAndSortedFindingsByName.size());
        assertTrue(groupedAndSortedFindingsByName.containsKey("Cross Site Scripting (Reflected)"));
        assertEquals(3, findingList.size());
        for (SecHubFinding secHubFinding : findingList) {
            assertEquals(ScanType.WEB_SCAN, secHubFinding.getType());
            assertEquals(Severity.HIGH, secHubFinding.getSeverity());
            assertEquals("Cross Site Scripting (Reflected)", secHubFinding.getName());
        }
    }

    @Test
    void when_findings_list_is_empty_then_filterFindingsForGeneralScan_must_return_empty_list() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();
        Map<Integer, List<HTMLScanResultCodeScanEntry>> codeScanEntries = new HashMap<>();
        List<Severity> severities = List.of(Severity.HIGH);

        /* execute */
        List<HTMLCodeScanEntriesSecHubFindingData> htmlSecHubFindingList = builderToTest.createCodeScanDataList(findings, codeScanEntries, severities);

        /* test */
        assertTrue(htmlSecHubFindingList.isEmpty());
    }

    @Test
    void when_findings_list_contains_1_CODE_SCAN_HIGH_findings_then_filterFindingsForGeneralScan_must_return_appropriate_list() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();
        SecHubFinding finding1 = new SecHubFinding();
        finding1.setId(0);
        finding1.setType(ScanType.CODE_SCAN);
        finding1.setSeverity(Severity.HIGH);
        finding1.setName("Deferring unsafe method \"Close\" on type \"*os.File\"");
        findings.add(finding1);
        
        SecHubFinding finding2 = new SecHubFinding();
        finding2.setId(1);
        finding2.setType(ScanType.CODE_SCAN);
        finding2.setSeverity(Severity.HIGH);
        finding2.setName("Deferring unsafe method \"Close\" on type \"*os.File\"");
        findings.add(finding2);
        
        SecHubFinding finding3 = new SecHubFinding();
        finding3.setId(2);
        finding3.setType(ScanType.CODE_SCAN);
        finding3.setSeverity(Severity.HIGH);
        finding3.setName("Deferring unsafe method \"Close\" on type \"*os.File\"");
        findings.add(finding3);

        Map<Integer, List<HTMLScanResultCodeScanEntry>> codeScanEntriesMap = new HashMap<>();
        codeScanEntriesMap.put(0, Arrays.asList(new HTMLScanResultCodeScanEntry()));
        codeScanEntriesMap.put(1, Arrays.asList(new HTMLScanResultCodeScanEntry()));
        codeScanEntriesMap.put(2, Arrays.asList(new HTMLScanResultCodeScanEntry()));

        List<Severity> severities = List.of(Severity.HIGH);

        /* execute */
        List<HTMLCodeScanEntriesSecHubFindingData> htmlSecHubFindingList = builderToTest.createCodeScanDataList(findings, codeScanEntriesMap, severities);

        /* test */
        assertEquals(1, htmlSecHubFindingList.size());
        assertEquals("Deferring unsafe method \"Close\" on type \"*os.File\"", htmlSecHubFindingList.get(0).getName());
        assertEquals(3, htmlSecHubFindingList.get(0).getEntryList().size());
    }

}
