package com.mercedesbenz.sechub.xraywrapper.report;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.model.Bom;
import org.cyclonedx.parsers.JsonParser;

import java.io.File;
import java.io.IOException;

import static org.cyclonedx.BomGeneratorFactory.createJson;

public class XrayWrapperReportWriter {

    public static void writeReport(Bom sbom, File report) throws XrayWrapperReportException {
        JsonNode jsonNode = createJson(CycloneDxSchema.Version.VERSION_14, sbom).toJsonNode();
        JsonParser jsonParser = new JsonParser();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(new File(report.toURI()), jsonNode);
        } catch (IOException e) {
            throw new XrayWrapperReportException("Could not write final xray report to file", e, XrayWrapperExitCode.IO_ERROR);
        }
        try {
            if (!jsonParser.isValid(report)) {
                throw new XrayWrapperReportException("CycloneDX report is not valid", XrayWrapperExitCode.FILE_NOT_VALID);
            }
        } catch (IOException e) {
            throw new XrayWrapperReportException("Could not validate report file", e, XrayWrapperExitCode.IO_ERROR);
        }
    }
}
