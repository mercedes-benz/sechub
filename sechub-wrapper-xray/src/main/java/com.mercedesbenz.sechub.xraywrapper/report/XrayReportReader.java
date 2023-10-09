package com.mercedesbenz.sechub.xraywrapper.report;

import static com.mercedesbenz.sechub.xraywrapper.util.ReportExtractor.fileExists;
import static com.mercedesbenz.sechub.xraywrapper.util.ReportExtractor.unzipReports;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class XrayReportReader {

    File cycloneReport;
    File securityReport;
    File sechubReport;
    private HashMap<String, XrayCycloneVulnerabilityBuilder> vulnerabilityHashMap;

    public void getFiles(String unzippedArchive, String pdsResultFile) throws XrayWrapperReportException {
        sechubReport = new File(pdsResultFile);
        if (!fileExists(unzippedArchive)) {
            // folder is zipped :)
            String zipArchive = unzippedArchive + ".zip";
            if (fileExists(zipArchive)) {
                unzipReports(Paths.get(zipArchive), Paths.get(unzippedArchive));
            } else {
                System.out.println("Error: could not find reports!");
            }
        }

        ArrayList<Path> cyclones = getFilesByName(unzippedArchive, "CycloneDX");
        if (!cyclones.isEmpty()) {
            cycloneReport = cyclones.get(0).toFile();
            if (pdsResultFile.isEmpty()) {
                String s = cycloneReport.toString().split("\\.")[0];
                sechubReport = new File(s + "-SecHub.json");
            }
        }
        ArrayList<Path> securityPath = getFilesByName(unzippedArchive, "Security");
        if (!securityPath.isEmpty()) {
            securityReport = securityPath.get(0).toFile();
        }
    }

    private ArrayList<Path> getFilesByName(String dir, String filename) {
        ArrayList<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(dir), ("*" + filename + "*.json"))) {
            dirStream.forEach(path -> paths.add(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return paths;
    }

    public void readSecurityReport() throws XrayWrapperReportException {
        XrayReportTransformer xrayReportTransformer = new XrayReportTransformer();
        JsonNode rootNode = xrayReportTransformer.getRootDataNode(securityReport);
        vulnerabilityHashMap = xrayReportTransformer.transformSecurityReport(rootNode);
    }

    public ObjectNode mapVulnerabilities() throws XrayWrapperReportException {
        return XrayVulnerabilityMapper.mapVulnerabilities(cycloneReport, vulnerabilityHashMap);
    }

    public void writeReport(ObjectNode rootNode) throws XrayWrapperReportException {
        XrayReportWriter.writeReport(rootNode, sechubReport);
    }
}