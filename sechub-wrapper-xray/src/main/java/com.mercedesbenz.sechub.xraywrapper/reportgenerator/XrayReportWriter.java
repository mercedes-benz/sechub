package com.mercedesbenz.sechub.xraywrapper.reportgenerator;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;

public class XrayReportWriter {
    public static void writeReport(ObjectNode rootObject, File report) throws XrayWrapperReportException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(new File(report.toURI()), rootObject);
        } catch (IOException e) {
            throw new XrayWrapperReportException("Error: could not write report to file", e, XrayWrapperExitCode.IO_ERROR);
        }
    }
}
