package com.mercedesbenz.sechub.xraywrapper.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

class XrayWrapperReportTransformerTest {

    XrayWrapperReportTransformer xrayWrapperReportTransformer;

    @BeforeEach
    public void beforeEach() {
        xrayWrapperReportTransformer = new XrayWrapperReportTransformer();
    }

    @Test
    public void test_getRootDataNode_null() {
        /* execute + test */
        Assert.assertThrows(IllegalArgumentException.class, () -> xrayWrapperReportTransformer.getRootDataNode(null));

    }

    @Test
    public void test_getRootDataNode() {
        /* prepare */
        File file = new File("src/test/resources/xray-report-examples/Docker_Security_Export.json");
        int numberOfVulnerabilities = 63;

        /* execute + test */
        JsonNode node = xrayWrapperReportTransformer.getRootDataNode(file);
        assertEquals(numberOfVulnerabilities, node.size());
    }

    @Test
    public void test_transformSecurityReport() {
        /* prepare */
        File file = new File("src/test/resources/xray-report-examples/Docker_Security_Export.json");
        JsonNode node = xrayWrapperReportTransformer.getRootDataNode(file);
        int numberOfVulnerabilities = 25;

        /* execute */
        HashMap<String, CycloneDXVulnerabilityBuilder> vulnerabilityHashMap = xrayWrapperReportTransformer.transformSecurityReport(node);

        /* test */
        assertEquals(numberOfVulnerabilities, vulnerabilityHashMap.size());
    }

    @Test
    public void test_transformSecurityReport_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> xrayWrapperReportTransformer.transformSecurityReport(null));
    }
}