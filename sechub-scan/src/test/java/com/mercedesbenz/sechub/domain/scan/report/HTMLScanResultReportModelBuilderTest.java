// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.core.io.Resource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.SecHubResultTrafficLightFilter;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.domain.scan.ScanDomainTestFileSupport;

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

    }

    @Test
    void metaData_set_as_optional_not_present_when_configuration_has_metadata_optional_null() {
        /* prepare */
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.YELLOW); // traffic light necessary to avoid illegal state exception
        when(scanSecHubReport.getMetaData()).thenReturn(Optional.ofNullable(null));

        /* execute */
        Map<String, Object> map = builderToTest.build(scanSecHubReport);

        /* test */
        @SuppressWarnings("unchecked")
        Optional<SecHubReportMetaData> metaData = (Optional<SecHubReportMetaData>) map.get("metaData");
        assertNotNull(metaData);
        assertFalse(metaData.isPresent());
    }

    @Test
    void metaData_set_as_optional_not_present_when_configuration_has_metadata_optional_defined() {
        /* prepare */
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.YELLOW); // traffic light necessary to avoid illegal state exception
        SecHubReportMetaData reportMetaData = mock(SecHubReportMetaData.class);
        when(scanSecHubReport.getMetaData()).thenReturn(Optional.ofNullable(reportMetaData));

        /* execute */
        Map<String, Object> map = builderToTest.build(scanSecHubReport);

        /* test */
        @SuppressWarnings("unchecked")
        Optional<SecHubReportMetaData> metaData = (Optional<SecHubReportMetaData>) map.get("metaData");
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

    @Test
    void trafficlight_red_set_display_block__others_are_none() {
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.RED);

        Map<String, Object> map = builderToTest.build(scanSecHubReport);
        assertEquals(SHOW_LIGHT, map.get("styleRed"));
        assertEquals(HIDE_LIGHT, map.get("styleYellow"));
        assertEquals(HIDE_LIGHT, map.get("styleGreen"));
    }

    @Test
    public void trafficlight_yellow_set_display_block__others_are_none() {
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.YELLOW);

        Map<String, Object> map = builderToTest.build(scanSecHubReport);
        assertEquals(HIDE_LIGHT, map.get("styleRed"));
        assertEquals(SHOW_LIGHT, map.get("styleYellow"));
        assertEquals(HIDE_LIGHT, map.get("styleGreen"));
    }

    @Test
    void trafficlight_green_set_display_block__others_are_none() {
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

    @Test
    void code_scan_support_set_and_not_null() {
        /* prepare */
        when(scanSecHubReport.getTrafficLight()).thenReturn(TrafficLight.RED);

        /* execute */
        Map<String, Object> map = builderToTest.build(scanSecHubReport);

        /* test */
        assertNotNull(map.get("codeScanSupport"));
        assertTrue(map.get("codeScanSupport") instanceof HTMLCodeScanDescriptionSupport);
    }

    @Test
    void createTrafficLightFindingGroups_empty_findings_3_groups_added_which_are_empty() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();

        /* execute */
        List<HTMLTrafficlightFindingGroup> created = builderToTest.createTrafficLightFindingGroups(findings);

        /* test */
        assertFalse(created.isEmpty());
        assertEquals(3, created.size());

        for (HTMLTrafficlightFindingGroup group : created) {
            assertTrue(group.getScanTypeFindingDataList().isEmpty());
        }
    }

    @Test
    void createTrafficLightFindingGroups_empty_findings_groups_contains_red_yellow_green_traffic_light_in_order() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();

        /* execute */
        List<HTMLTrafficlightFindingGroup> created = builderToTest.createTrafficLightFindingGroups(findings);

        /* test */
        assertFalse(created.isEmpty());
        assertEquals(3, created.size());

        Set<TrafficLight> set = new LinkedHashSet<>();
        for (HTMLTrafficlightFindingGroup group : created) {
            set.add(group.getTrafficLight());
        }

        assertEquals(3, set.size());
        Iterator<TrafficLight> it = set.iterator();
        TrafficLight light1 = it.next();
        TrafficLight light2 = it.next();
        TrafficLight light3 = it.next();

        assertEquals(TrafficLight.RED, light1);
        assertEquals(TrafficLight.YELLOW, light2);
        assertEquals(TrafficLight.GREEN, light3);

    }

    @Test
    void createTrafficLightFindingGroups_3_findings_groups_contains_findings_grouped_and_sorted() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();
        SecHubFinding finding1 = new SecHubFinding();
        finding1.setId(0);
        finding1.setType(ScanType.CODE_SCAN);
        finding1.setSeverity(Severity.LOW);
        findings.add(finding1);

        SecHubFinding finding2 = new SecHubFinding();
        finding2.setId(1);
        finding2.setType(ScanType.CODE_SCAN);
        finding2.setSeverity(Severity.MEDIUM);
        findings.add(finding2);

        SecHubFinding finding3 = new SecHubFinding();
        finding3.setId(2);
        finding3.setType(ScanType.SECRET_SCAN);
        finding3.setSeverity(Severity.HIGH);
        findings.add(finding3);

        SecHubFinding finding4 = new SecHubFinding();
        finding4.setId(3);
        finding4.setType(ScanType.LICENSE_SCAN);
        finding4.setSeverity(Severity.INFO);
        findings.add(finding4);

        SecHubFinding finding5 = new SecHubFinding();
        finding5.setId(4);
        finding5.setType(ScanType.LICENSE_SCAN);
        finding5.setSeverity(Severity.LOW);
        findings.add(finding5);

        /* execute */
        List<HTMLTrafficlightFindingGroup> created = builderToTest.createTrafficLightFindingGroups(findings);

        /* test */
        for (HTMLTrafficlightFindingGroup group : created) {
            Collection<HTMLTrafficlightScanTypeFindingData> findingDataList = group.getScanTypeFindingDataList();
            TrafficLight trafficLight = group.getTrafficLight();

            Iterator<HTMLTrafficlightScanTypeFindingData> findingDataIt = findingDataList.iterator();
            if (TrafficLight.RED.equals(trafficLight)) {

                assertEquals(1, findingDataList.size());
                HTMLTrafficlightScanTypeFindingData entry = findingDataIt.next();
                assertEquals(ScanType.SECRET_SCAN, entry.getScanType());

                List<SecHubFinding> relatedFindings = entry.getRelatedFindings();
                assertEquals(1, relatedFindings.size());
                assertEquals(finding3, relatedFindings.get(0));

            } else if (TrafficLight.YELLOW.equals(trafficLight)) {
                assertEquals(1, findingDataList.size());
                HTMLTrafficlightScanTypeFindingData entry = findingDataIt.next();

                assertEquals(ScanType.CODE_SCAN, entry.getScanType());

                List<SecHubFinding> relatedFindings = entry.getRelatedFindings();
                assertEquals(1, relatedFindings.size());
                assertEquals(finding2, relatedFindings.get(0));

            } else if (TrafficLight.GREEN.equals(trafficLight)) {
                assertEquals(2, findingDataList.size());

                HTMLTrafficlightScanTypeFindingData entry1 = findingDataIt.next();
                List<SecHubFinding> relatedFindings1 = entry1.getRelatedFindings();
                assertEquals(1, relatedFindings1.size());
                assertEquals(finding1, relatedFindings1.get(0));
                assertEquals(ScanType.CODE_SCAN, entry1.getScanType());

                HTMLTrafficlightScanTypeFindingData entry2 = findingDataIt.next();
                List<SecHubFinding> relatedFindings2 = entry2.getRelatedFindings();

                assertEquals(ScanType.LICENSE_SCAN, entry2.getScanType());
                assertEquals(2, relatedFindings2.size());
                /* @formatter:off
                 * check sorting:
                 *
                 * - finding 5 added last, scan type:low
                 * - finding 4 added before finding 5, scan type: info
                 *
                 * Scan type "info" is less important than "low" -> finding 4 must be at the end
                 * @formatter:on
                 */
                assertEquals(finding4, relatedFindings2.get(1));
                assertEquals(finding5, relatedFindings2.get(0));

            } else {
                fail("Unexpected value: " + trafficLight);
            }
        }

    }

    @Test
    void createScanTypeSummaries_empty_findings() {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();

        /* execute */
        List<HTMLScanTypSummary> created = builderToTest.createScanTypeSummaries(findings);

        /* test */
        assertTrue(created.isEmpty());

    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void createScanTypeSummaries_same_scan_type_3_codescan_findings_creates_summary(ScanType scanType) {
        /* prepare */
        List<SecHubFinding> findings = new ArrayList<>();
        SecHubFinding finding1 = new SecHubFinding();
        finding1.setId(0);
        finding1.setType(scanType);
        finding1.setSeverity(Severity.LOW);
        findings.add(finding1);

        SecHubFinding finding2 = new SecHubFinding();
        finding2.setId(1);
        finding2.setType(scanType);
        finding2.setSeverity(Severity.MEDIUM);
        findings.add(finding2);

        SecHubFinding finding3 = new SecHubFinding();
        finding3.setId(2);
        finding3.setType(scanType);
        finding3.setSeverity(Severity.HIGH);
        findings.add(finding3);

        /* execute */
        List<HTMLScanTypSummary> created = builderToTest.createScanTypeSummaries(findings);

        /* test */
        assertEquals(1, created.size());
        Iterator<HTMLScanTypSummary> iterator = created.iterator();
        HTMLScanTypSummary summary1 = iterator.next();

        assertEquals(scanType, summary1.getScanType());

        assertEquals(0, summary1.getCriticalSeverityCount());
        assertEquals(1, summary1.getHighSeverityCount());
        assertEquals(1, summary1.getMediumSeverityCount());
        assertEquals(1, summary1.getLowSeverityCount());
        assertEquals(0, summary1.getUnclassifiedSeverityCount());
        assertEquals(0, summary1.getInfoSeverityCount());
    }

    @Test
    void createScanTypeSummaries_3_codescan_findings_same_name_creates_summary() {
        /* prepare */
        ScanType scanType = ScanType.CODE_SCAN;
        List<SecHubFinding> findings = new ArrayList<>();
        SecHubFinding finding1 = new SecHubFinding();
        finding1.setId(0);
        finding1.setName("name1");
        finding1.setType(scanType);
        finding1.setSeverity(Severity.LOW);
        findings.add(finding1);

        SecHubFinding finding2 = new SecHubFinding();
        finding2.setId(1);
        finding2.setName("name1");
        finding2.setType(scanType);
        finding2.setSeverity(Severity.LOW);
        findings.add(finding2);

        SecHubFinding finding3 = new SecHubFinding();
        finding3.setId(2);
        finding3.setName("name1");
        finding3.setType(scanType);
        finding3.setSeverity(Severity.LOW);
        findings.add(finding3);

        /* execute */
        List<HTMLScanTypSummary> created = builderToTest.createScanTypeSummaries(findings);

        /* test */
        assertEquals(1, created.size());
        Iterator<HTMLScanTypSummary> iterator = created.iterator();
        HTMLScanTypSummary summary1 = iterator.next();

        assertEquals(scanType, summary1.getScanType());

        assertEquals(0, summary1.getCriticalSeverityCount());
        assertEquals(0, summary1.getHighSeverityCount());
        assertEquals(0, summary1.getMediumSeverityCount());
        assertEquals(3, summary1.getLowSeverityCount());
        assertEquals(0, summary1.getUnclassifiedSeverityCount());
        assertEquals(0, summary1.getInfoSeverityCount());
        assertEquals(3, summary1.getTotalCount());
    }

}
