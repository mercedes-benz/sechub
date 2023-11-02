package com.mercedesbenz.sechub.wrapper.xray.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.cyclonedx.model.Bom;

import com.fasterxml.jackson.databind.JsonNode;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.util.ZipFileExtractor;

public class XrayWrapperReportReader {

    File cycloneReport;
    File securityReport;
    File xrayPdsReport;

    private HashMap<String, CycloneDXVulnerabilityHelper> cycloneDXVulnerabilityHashMap;

    public void findXrayReportsInArchive(String unzippedArchive, String pdsResultFile) throws XrayWrapperReportException {
        xrayPdsReport = new File(pdsResultFile);
        if (!ZipFileExtractor.fileExists(unzippedArchive)) {
            // folder with reports is zipped
            String zipArchive = unzippedArchive + ".zip";
            if (ZipFileExtractor.fileExists(zipArchive)) {
                ZipFileExtractor.unzipFile(Paths.get(zipArchive), Paths.get(unzippedArchive));
            } else {
                throw new XrayWrapperReportException("File with reports does not exist");
            }
        }

        ArrayList<Path> cyclones = getFilesByName(unzippedArchive, "CycloneDX");
        if (!cyclones.isEmpty()) {
            cycloneReport = cyclones.get(0).toFile();
            if (pdsResultFile.isEmpty()) {
                String s = cycloneReport.toString().split("\\.")[0];
                xrayPdsReport = new File(s + "-SecHub.json");
            }
        }
        ArrayList<Path> securityPath = getFilesByName(unzippedArchive, "Security");
        if (!securityPath.isEmpty()) {
            securityReport = securityPath.get(0).toFile();
        }
    }

    private ArrayList<Path> getFilesByName(String dir, String filename) throws XrayWrapperReportException {
        ArrayList<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(dir), ("*" + filename + "*.json"))) {
            dirStream.forEach(paths::add);
        } catch (IOException e) {
            throw new XrayWrapperReportException("Could not find Security and CycloneDX reports", e);
        }
        return paths;
    }

    public void readSecurityReport() throws XrayWrapperException {
        XrayWrapperReportParser xrayWrapperReportParser = new XrayWrapperReportParser();
        JsonNode rootNode = xrayWrapperReportParser.getRootDataNode(securityReport);
        cycloneDXVulnerabilityHashMap = xrayWrapperReportParser.transformSecurityReport(rootNode);
    }

    public Bom mapVulnerabilities() throws XrayWrapperReportException {
        return XrayWrapperReportVulnerabilityMapper.mapVulnerabilities(cycloneReport, cycloneDXVulnerabilityHashMap);
    }

    public void writeReport(Bom sbom) throws XrayWrapperReportException {
        XrayWrapperReportWriter.writeReport(sbom, xrayPdsReport);
    }

    public HashMap<String, CycloneDXVulnerabilityHelper> getCycloneDXVulnerabilityHashMap() {
        return cycloneDXVulnerabilityHashMap;
    }
}