// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubResult;
import com.mercedesbenz.sechub.commons.model.SecHubResultTrafficLightFilter;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
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
        assertTrue(map.get("codeScanSupport") instanceof HtmlCodeScanDescriptionSupport);

    }

}
