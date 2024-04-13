// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.Map;

import org.cyclonedx.model.vulnerability.Vulnerability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperJSONConverter;

class XrayWrapperReportTransformerTest {

    XrayWrapperReportTransformer xrayWrapperReportTransformerToTest;

    @BeforeEach
    void beforeEach() {
        xrayWrapperReportTransformerToTest = new XrayWrapperReportTransformer();
    }

    @Test
    void transformSecurityReport_number_of_vulnerabilities() throws XrayWrapperException {
        /* prepare */
        File file = new File("src/test/resources/xray-report-examples/Docker_Security_Export.json");
        JsonNode node = XrayWrapperJSONConverter.get().readJSONFromFile(file).get("data");
        int expectedNumberOfVulnerabilities = 25;

        /* execute */
        Map<String, Vulnerability> vulnerabilityHashMap = xrayWrapperReportTransformerToTest.transformVulnerabilitiesFromSecurityReport(node);

        /* test */
        assertEquals(expectedNumberOfVulnerabilities, vulnerabilityHashMap.size());
    }

    @Test
    void transformSecurityReport_null_securityReport_throws_xrayWrapperReportException() {
        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperReportException.class,
                () -> xrayWrapperReportTransformerToTest.transformVulnerabilitiesFromSecurityReport(null));

        /* test */
        assertEquals("Error occurred during report handling: Xray Security Report node was NULL", exception.getMessage());
    }
}