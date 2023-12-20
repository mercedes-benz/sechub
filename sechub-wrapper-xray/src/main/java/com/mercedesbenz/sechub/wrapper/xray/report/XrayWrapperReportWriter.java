// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.report;

import static org.cyclonedx.BomGeneratorFactory.createJson;

import java.io.File;
import java.io.IOException;

import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.model.Bom;
import org.cyclonedx.parsers.JsonParser;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperJSONConverter;

public class XrayWrapperReportWriter {

    public void writeReport(Bom sbom, File report) throws XrayWrapperReportException {
        if (sbom == null || report == null) {
            throw new IllegalStateException("SBOM or report file can not be NULL");
        }
        JsonNode jsonNode = createJson(CycloneDxSchema.Version.VERSION_14, sbom).toJsonNode();
        JsonParser jsonParser = new JsonParser();
        ObjectMapper mapper = XrayWrapperJSONConverter.get().getMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(new File(report.toURI()), jsonNode);
        } catch (IOException e) {
            throw new XrayWrapperCreateCycloneDXReportException("Could not write final Xray report to file", e);
        }
        try {
            if (!jsonParser.isValid(report)) {
                throw new XrayWrapperCreateCycloneDXReportException("CycloneDX report is not valid");
            }
        } catch (IOException e) {
            throw new XrayWrapperCreateCycloneDXReportException("Could not validate report file", e);
        }
    }
}
