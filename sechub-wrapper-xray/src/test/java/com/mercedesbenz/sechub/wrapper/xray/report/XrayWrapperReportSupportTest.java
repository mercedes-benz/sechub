// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.report;

import static org.cyclonedx.BomGeneratorFactory.createJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
    void collectReportFile_file_not_exist_throws_xrayWrapperReportException() {
        /* execute */
        XrayWrapperReportException exception = assertThrows(XrayWrapperReportException.class,
                () -> reportSupportToTest.collectXrayReportsInArchive("invalid", ""));

        /* test */
        assertEquals("Error occurred during report handling: File with reports does not exist", exception.getMessage());
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
    void readSecurityReport_security_report_null_throws_illegalArgumentException() {
        /* execute */
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> reportSupportToTest.readSecurityReport(null));

        /* test */
        assertEquals("argument \"file\" is null", exception.getMessage());
    }

    @Test
    void mapVulnerabilities_valid_vulnerabilities() throws IOException, XrayWrapperException {
        /* prepare */
        File securityReport = new File("src/test/resources/xray-report-examples/Docker_Security_Export.json");
        File cycloneDXReport = new File("src/test/resources/xray-report-examples/Docker_SBOM_Export_CycloneDX.json");
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
    void mapVulnerabilities_null_cycloneDX_report_throws_xrayWrapperReportException() throws XrayWrapperReportException {
        /* prepare */
        Map vulnerabiliityMap = Collections.emptyMap();

        /* execute */
        XrayWrapperReportException exception = assertThrows(XrayWrapperReportException.class,
                () -> reportSupportToTest.mapVulnerabilities(null, vulnerabiliityMap));

        /* test */
        assertEquals("Error occurred during report handling: Cannot parse JSON: argument \"src\" is null", exception.getMessage());

    }
}