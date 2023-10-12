package com.mercedesbenz.sechub.xraywrapper.report;

import static org.cyclonedx.BomGeneratorFactory.createJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.model.Bom;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class XrayWrapperReportReaderTest {

    XrayWrapperReportReader reportReader;
    ObjectMapper mapper;

    @BeforeEach
    public void beforeEach() {
        reportReader = new XrayWrapperReportReader();
        mapper = new ObjectMapper();
    }

    @Test
    public void test_getFiles() throws IOException {
        /* prepare */
        String source = "src/test/resources/xray-report-examples";
        String resultFile = "resultfile";
        String cycloneReport = "Docker_SBOM_Export_CycloneDX.json";
        String securityReport = "Docker_Security_Export.json";

        /* execute */
        reportReader.getFiles(source, resultFile);

        /* test */
        String cycloneName = reportReader.cycloneReport.getName();
        String securityName = reportReader.securityReport.getName();
        String sechubReport = reportReader.sechubReport.getName();
        assertEquals(resultFile, sechubReport);
        assertEquals(cycloneReport, cycloneName);
        assertEquals(securityReport, securityName);
    }

    @Test
    void test_getFile_null() throws IOException {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> reportReader.getFiles(null, null));
    }

    @Test
    void test_readSecurityReport() throws IOException {
        /* prepare */
        String source = "src/test/resources/xray-report-examples";
        reportReader.getFiles(source, "");

        /* execute + test */
        reportReader.readSecurityReport();
    }

    @Test
    public void test_readSecurityReport_null() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> reportReader.readSecurityReport());
    }

    @Test
    public void test_mapVulnerabilities() throws IOException {
        /* prepare */
        String source = "src/test/resources/xray-report-examples";
        reportReader.getFiles(source, "");
        reportReader.readSecurityReport();
        File target = new File("src/test/resources/xray-sechub-report-examples/Docker_SBOM_Export_CycloneDX.json");
        ObjectNode expectedNode = (ObjectNode) mapper.readTree(target);

        /* execute */
        Bom bom = reportReader.mapVulnerabilities();

        /* test */
        JsonNode root = createJson(CycloneDxSchema.Version.VERSION_14, bom).toJsonNode();
        Assertions.assertEquals(expectedNode.toString(), root.toString());
    }

    @Test
    public void test_mapVulnerabilities_null() {
        Assert.assertThrows(IllegalArgumentException.class, () -> reportReader.mapVulnerabilities());
    }

}