// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

class SerecoProjectDataFalsePositiveFinderTest {

    private SerecoProjectDataFalsePositiveFinder finderToTest;

    private WebScanProjectDataFalsePositiveStrategy webScanProjectDataStrategy;

    @BeforeEach
    void beforeEach() {
        finderToTest = new SerecoProjectDataFalsePositiveFinder();

        webScanProjectDataStrategy = mock(WebScanProjectDataFalsePositiveStrategy.class);

        finderToTest.webScanProjectDataStrategy = webScanProjectDataStrategy;
    }

    @Test
    void vulnerability_invalid_no_strategy_is_ever_called() {
        /* prepare */
        SerecoVulnerability vuln1 = null;
        SerecoVulnerability vuln2 = new SerecoVulnerability();
        SerecoVulnerability vuln3 = new SerecoVulnerability();
        vuln3.setScanType(ScanType.WEB_SCAN);

        // not null
        FalsePositiveProjectData projectData = new FalsePositiveProjectData();

        // not null or empty
        Map<String, Pattern> patternMap = new HashMap<>();
        patternMap.put("key", mock(Pattern.class));

        /* execute */
        boolean result1 = finderToTest.isFound(vuln1, projectData, patternMap);
        boolean result2 = finderToTest.isFound(vuln2, projectData, patternMap);
        boolean result3 = finderToTest.isFound(vuln3, projectData, patternMap);

        /* test */
        verify(webScanProjectDataStrategy, never()).isFalsePositive(vuln1, projectData, patternMap);
        verify(webScanProjectDataStrategy, never()).isFalsePositive(vuln2, projectData, patternMap);
        verify(webScanProjectDataStrategy, never()).isFalsePositive(vuln3, projectData, patternMap);

        assertFalse(result1);
        assertFalse(result2);
        assertFalse(result3);
    }

    @Test
    void projectData_null_no_strategy_is_ever_called() {
        /* prepare */
        SerecoVulnerability vuln = new SerecoVulnerability();
        vuln.setScanType(ScanType.WEB_SCAN);
        vuln.setType("type");

        FalsePositiveProjectData projectData = null;
        // not null or empty
        Map<String, Pattern> patternMap = new HashMap<>();
        patternMap.put("key", mock(Pattern.class));

        /* execute */
        boolean result = finderToTest.isFound(vuln, projectData, patternMap);

        /* test */
        verify(webScanProjectDataStrategy, never()).isFalsePositive(vuln, projectData, patternMap);
        assertFalse(result);
    }

    @Test
    void patternMap_null_or_empty_no_strategy_is_ever_called() {
        /* prepare */
        SerecoVulnerability vuln = new SerecoVulnerability();
        vuln.setScanType(ScanType.WEB_SCAN);
        vuln.setType("type");

        FalsePositiveProjectData projectData = new FalsePositiveProjectData();

        Map<String, Pattern> patternMapNull = null;
        Map<String, Pattern> patternMapEmpty = new HashMap<>();

        /* execute */
        boolean result1 = finderToTest.isFound(vuln, projectData, patternMapNull);
        boolean result2 = finderToTest.isFound(vuln, projectData, patternMapEmpty);

        /* test */
        verify(webScanProjectDataStrategy, never()).isFalsePositive(vuln, projectData, patternMapNull);
        verify(webScanProjectDataStrategy, never()).isFalsePositive(vuln, projectData, patternMapEmpty);

        assertFalse(result1);
        assertFalse(result2);
    }

    @ParameterizedTest
    @EnumSource(ScanType.class)
    void scantypes_are_handle_correctly(ScanType scantype) {
        // This test needs to be updated if other scan types are supported with project
        // data in the future

        /* prepare */
        SerecoVulnerability vuln = new SerecoVulnerability();
        vuln.setScanType(scantype);
        vuln.setType("type");

        FalsePositiveProjectData projectData = new FalsePositiveProjectData();

        Map<String, Pattern> patternMap = new HashMap<>();
        patternMap.put("key", mock(Pattern.class));

        when(webScanProjectDataStrategy.isFalsePositive(vuln, projectData, patternMap)).thenReturn(true);

        /* execute */
        boolean result = finderToTest.isFound(vuln, projectData, patternMap);

        /* test */
        if (scantype == ScanType.WEB_SCAN) {
            verify(webScanProjectDataStrategy, times(1)).isFalsePositive(vuln, projectData, patternMap);
            assertTrue(result);
        } else {
            verify(webScanProjectDataStrategy, never()).isFalsePositive(vuln, projectData, patternMap);
            assertFalse(result);
        }
    }

}
