// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.TrafficLight;

class HTMLReportHelperTest {

    private HTMLReportHelper helperToTest;

    @BeforeEach
    void beforeEach() {
        helperToTest = new HTMLReportHelper();
    }

    @Test
    void createSummaryTableAnkerIdForRed_scantype_codescan() {
        /* just check this works - we use internally another method which is tested in a generic way */
        assertEquals("#redCodeScanTable", helperToTest.createSummaryTableAnkerLinkForRed(ScanType.CODE_SCAN));
    }
    
    @Test
    void createSummaryTableAnkerIdForYellow_scantype_webscan() {
        /* just check this works - we use internally another method which is tested in a generic way */
        assertEquals("#yellowWebScanTable", helperToTest.createSummaryTableAnkerLinkForYellow(ScanType.WEB_SCAN));
    }
    
    @Test
    void createSummaryTableAnkerIdForGreen_scantype_infrascan() {
        /* just check this works - we use internally another method which is tested in a generic way */
        assertEquals("#greenInfraScanTable", helperToTest.createSummaryTableAnkerLinkForGreen(ScanType.INFRA_SCAN));
    }
    
    @EnumSource(TrafficLight.class)
    @ParameterizedTest
    void createSummaryTableAnkerId_for_given_traffic_light_and_every_scantype(TrafficLight trafficLight) {
        
        String expectedPrefix = trafficLight.name().toLowerCase();
        
        for (ScanType scanType : ScanType.values()) {
            String scanTypeString = scanType.getId();
            scanTypeString = scanTypeString.substring(0, 1).toUpperCase() + scanTypeString.substring(1);

            String expected = expectedPrefix + scanTypeString + "Table";

            assertEquals(expected, helperToTest.createSummaryTableAnkerId(trafficLight, scanType));
        }
    }

    @Test
    /*
     * This is an explicit test for dedicated strings - if the parameterized unit tests
     * would calculate it wrong...
     */
    void createSummaryTableAnkerId_codeScanWithTrafficLights() {

        assertEquals("redCodeScanTable", helperToTest.createSummaryTableAnkerId(TrafficLight.RED, ScanType.CODE_SCAN));
        assertEquals("yellowCodeScanTable", helperToTest.createSummaryTableAnkerId(TrafficLight.YELLOW, ScanType.CODE_SCAN));
        assertEquals("greenCodeScanTable", helperToTest.createSummaryTableAnkerId(TrafficLight.GREEN, ScanType.CODE_SCAN));

        /* special case -not really supported in template, but supported by helper */
        assertEquals("offCodeScanTable", helperToTest.createSummaryTableAnkerId(TrafficLight.OFF, ScanType.CODE_SCAN));
    }

    @ParameterizedTest
    @EnumSource(SecHubMessageType.class)
    void getMessageTypeAsHTMLIcon_by_wellknown_message_type_results_in_unicode_icon(SecHubMessageType type) {
        /* execute */
        String icon = helperToTest.getMessageTypeAsHTMLIcon(type);

        /* test */
        switch (type) {
        case ERROR:
            assertEquals("&#128711;", icon); // prohibit
            break;
        case INFO:
            assertEquals("&#128712;", icon); // circle info
            break;
        case WARNING:
            assertEquals("&#9888;", icon); // warning
            break;
        default:
            fail("Unexpected type - new ?");

        }
    }

    @ParameterizedTest
    @NullSource
    void getMessageTypeAsHTMLIcon_by_unknown_message_type_results_in_empty_string(SecHubMessageType type) {
        /* execute */
        String icon = helperToTest.getMessageTypeAsHTMLIcon(type);

        /* test */
        assertEquals("", icon);
    }

}
