package com.mercedesbenz.sechub.xraywrapper.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

class XrayWrapperReportParserTest {

    XrayWrapperReportParser xrayWrapperReportParser;

    @BeforeEach
    void beforeEach() {
        xrayWrapperReportParser = new XrayWrapperReportParser();
    }

    @Test
    void getRootDataNode_throws_illegalArgumentException() {
        /* execute + test */
        Assert.assertThrows(IllegalArgumentException.class, () -> xrayWrapperReportParser.getRootDataNode(null));
    }

    @Test
    void getRootDataNode_throws_xrayWrapperReportException() {
        /* prepare */
        File file = new File("src/test/resources/invalid-json-examples/invalid_json.json");
        /* execute + test */
        Assert.assertThrows(XrayWrapperReportException.class, () -> xrayWrapperReportParser.getRootDataNode(file));
    }

    @Test
    void getRootDataNode_return_size_of_node() {
        /* prepare */
        File file = new File("src/test/resources/xray-report-examples/Docker_Security_Export.json");
        // some vulnerabilities appear multiple times
        int numberOfVulnerabilities = 63;

        /* execute + test */
        JsonNode node = xrayWrapperReportParser.getRootDataNode(file);
        assertEquals(numberOfVulnerabilities, node.size());
    }

    @Test
    void transformSecurityReport_number_of_vulnerabilities() {
        /* prepare */
        File file = new File("src/test/resources/xray-report-examples/Docker_Security_Export.json");
        JsonNode node = xrayWrapperReportParser.getRootDataNode(file);
        int numberOfVulnerabilities = 25;

        /* execute */
        HashMap<String, CycloneDXVulnerabilityHelper> vulnerabilityHashMap = xrayWrapperReportParser.transformSecurityReport(node);

        /* test */
        assertEquals(numberOfVulnerabilities, vulnerabilityHashMap.size());
    }

    @Test
    void transformSecurityReport_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> xrayWrapperReportParser.transformSecurityReport(null));
    }
}