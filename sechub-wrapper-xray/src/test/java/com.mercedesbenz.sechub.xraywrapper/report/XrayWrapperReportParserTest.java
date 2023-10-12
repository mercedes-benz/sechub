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
    public void beforeEach() {
        xrayWrapperReportParser = new XrayWrapperReportParser();
    }

    @Test
    public void test_getRootDataNode_null() {
        /* execute + test */
        Assert.assertThrows(IllegalArgumentException.class, () -> xrayWrapperReportParser.getRootDataNode(null));

    }

    @Test
    public void test_getRootDataNode() {
        /* prepare */
        File file = new File("src/test/resources/xray-report-examples/Docker_Security_Export.json");
        int numberOfVulnerabilities = 63;

        /* execute + test */
        JsonNode node = xrayWrapperReportParser.getRootDataNode(file);
        assertEquals(numberOfVulnerabilities, node.size());
    }

    @Test
    public void test_transformSecurityReport() {
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
    public void test_transformSecurityReport_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> xrayWrapperReportParser.transformSecurityReport(null));
    }
}