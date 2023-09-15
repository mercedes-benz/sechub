package com.mercedesbenz.sechub.xraywrapper.reportgenerator;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class XrayReportReaderTest {

    XrayReportReader reportReader;
    ObjectMapper mapper;

    @BeforeEach
    public void beforeEach() {
        reportReader = new XrayReportReader();
        mapper = new ObjectMapper();
    }

    @Test
    public void testReadReport() {
        // prepare
        String source = "src/test/resources/xray-report-examples";
        File target = new File("src/test/resources/xray-sechub-report-examples/Docker_SBOM_Export_CycloneDX.json");

        // execute
        try {
            reportReader.readReport(source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // assert
        source = source + "/Docker_SBOM_Export_CycloneDX-SecHub.json";
        File src = new File(source);
        try {
            Assertions.assertEquals(mapper.readTree(target), mapper.readTree(src));
            src.delete();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}