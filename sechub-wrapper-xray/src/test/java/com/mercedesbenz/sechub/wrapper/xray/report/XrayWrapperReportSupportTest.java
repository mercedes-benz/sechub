package com.mercedesbenz.sechub.wrapper.xray.report;

import static org.cyclonedx.BomGeneratorFactory.createJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperJSONConverter;

class XrayWrapperReportSupportTest {

    XrayWrapperReportSupport reportSupportToTest;
    ObjectMapper mapper;

    @BeforeEach
    void beforeEach() {
        reportSupportToTest = new XrayWrapperReportSupport();
        mapper = XrayWrapperJSONConverter.get().getMapper();
    }

    @Test
    void collectReportFiles_get_valid_reports() throws XrayWrapperReportException {
        /* prepare */
        String source = "src/test/resources/xray-report-examples";
        String resultFile = "resultFile";
        String cycloneReport = "Docker_SBOM_Export_CycloneDX.json";
        String securityReport = "Docker_Security_Export.json";

        /* execute */
        XrayWrapperReportSupport.XrayReportFiles reportFiles = reportSupportToTest.collectXrayReportsInArchive(source, resultFile);

        /* test */
        String cycloneName = reportFiles.cycloneReport().getName();
        String securityName = reportFiles.securityReport().getName();
        String xrayPdsReport = reportFiles.xrayPdsReport().getName();
        assertEquals(resultFile, xrayPdsReport);
        assertEquals(cycloneReport, cycloneName);
        assertEquals(securityReport, securityName);
    }

    @Test
    void collectReportFile_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> reportSupportToTest.collectXrayReportsInArchive(null, null));
    }

    @Test
    void readSecurityReport_read_vulnerabilities_from_security_report() throws XrayWrapperException {
        /* prepare */
        String source = "src/test/resources/xray-report-examples/Docker_Security_Export.json";
        File securityReport = new File(source);

        /* execute */
        Map vulnerabiliityMap = reportSupportToTest.readSecurityReport(securityReport);

        /* test */
        assertEquals(25, vulnerabiliityMap.size());
    }

    @Test
    void readSecurityReport_throws_illegalArgumentException() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> reportSupportToTest.readSecurityReport(null));
    }

    @Test
    void mapVulnerabilities_valid_vulnerabilities() throws IOException, XrayWrapperException {
        /* prepare */
        String secReportFileName = "src/test/resources/xray-report-examples/Docker_Security_Export.json";
        String cycloneDXReportFilename = "src/test/resources/xray-report-examples/Docker_SBOM_Export_CycloneDX.json";
        File securityReport = new File(secReportFileName);
        File cycloneDXReport = new File(cycloneDXReportFilename);
        Map vulnerabiliityMap = reportSupportToTest.readSecurityReport(securityReport);
        File target = new File("src/test/resources/xray-sechub-report-examples/Docker_SBOM_Export_CycloneDX.json");
        ObjectNode expectedNode = (ObjectNode) mapper.readTree(target);

        /* execute */
        Bom bom = reportSupportToTest.mapVulnerabilities(cycloneDXReport, vulnerabiliityMap);

        /* test */
        JsonNode root = createJson(CycloneDxSchema.Version.VERSION_14, bom).toJsonNode();
        assertEquals(expectedNode.toString(), root.toString());
    }

    @Test
    void mapVulnerabilities_throws_illegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> reportSupportToTest.mapVulnerabilities(null, null));
    }
}