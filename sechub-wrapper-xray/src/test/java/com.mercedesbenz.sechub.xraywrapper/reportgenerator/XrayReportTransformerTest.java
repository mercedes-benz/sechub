package com.mercedesbenz.sechub.xraywrapper.reportgenerator;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

class XrayReportTransformerTest {

    XrayReportTransformer xrayReportTransformer;

    @BeforeEach
    public void beforeEach() {
        xrayReportTransformer = new XrayReportTransformer();
    }

    @Test
    public void test_getRootDataNode_null() {
        /* execute + test */
        Assert.assertThrows(IllegalArgumentException.class, () -> xrayReportTransformer.getRootDataNode(null));

    }

    @Test
    public void test_getRootDataNode() throws IOException {
        /* prepare */
        File file = new File("src/test/resources/xray-sechub-report-examples/Docker_SBOM_Export_CycloneDX.json");

        /* execute + test */
        JsonNode node = xrayReportTransformer.getRootDataNode(file);
        // assertEquals("2023-09-07T14:12:12+02:00", node.get("timestamp").asText());
    }

}