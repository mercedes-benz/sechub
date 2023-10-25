package com.mercedesbenz.sechub.xraywrapper.report;

import static org.cyclonedx.BomGeneratorFactory.createJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.model.Bom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class XrayWrapperReportReaderTest {

    XrayWrapperReportReader reportReader;
    ObjectMapper mapper;

    @BeforeEach
    void beforeEach() {
        reportReader = new XrayWrapperReportReader();
        mapper = new ObjectMapper();
    }

    @Test
    void getReportFiles_get_valid_reports() {
        /* prepare */
        String source = "src/test/resources/xray-report-examples";
        String resultFile = "resultfile";
        String cycloneReport = "Docker_SBOM_Export_CycloneDX.json";
        String securityReport = "Docker_Security_Export.json";

        /* execute */
        reportReader.getReportFiles(source, resultFile);

        /* test */
        String cycloneName = reportReader.cycloneReport.getName();
        String securityName = reportReader.securityReport.getName();
        String sechubReport = reportReader.sechubReport.getName();
        assertEquals(resultFile, sechubReport);
        assertEquals(cycloneReport, cycloneName);
        assertEquals(securityReport, securityName);
    }

    @Test
    void getReportFile_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> reportReader.getReportFiles(null, null));
    }

    @Test
    void readSecurityReport_read_vulnerabilities_from_security_report() {
        /* prepare */
        String source = "src/test/resources/xray-report-examples";
        reportReader.getReportFiles(source, "");

        /* execute */
        reportReader.readSecurityReport();

        /* test */
        assertEquals(25, reportReader.getCycloneDXVulnerabilityHashMap().size());
    }

    @Test
    void readSecurityReport_throws_illegalArgumentException() {
        /* execute + test */
        assertThrows(IllegalArgumentException.class, () -> reportReader.readSecurityReport());
    }

    @Test
    void mapVulnerabilities_valid_vulnerabilities() throws IOException {
        /* prepare */
        String source = "src/test/resources/xray-report-examples";
        reportReader.getReportFiles(source, "");
        reportReader.readSecurityReport();
        File target = new File("src/test/resources/xray-sechub-report-examples/Docker_SBOM_Export_CycloneDX.json");
        ObjectNode expectedNode = (ObjectNode) mapper.readTree(target);

        /* execute */
        Bom bom = reportReader.mapVulnerabilities();

        /* test */
        JsonNode root = createJson(CycloneDxSchema.Version.VERSION_14, bom).toJsonNode();
        assertEquals(expectedNode.toString(), root.toString());
    }

    @Test
    void mapVulnerabilities_throws_illegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> reportReader.mapVulnerabilities());
    }

}