package com.mercedesbenz.sechub.xraywrapper.report;

import static com.mercedesbenz.sechub.xraywrapper.util.ZipFileExtractor.fileExists;
import static com.mercedesbenz.sechub.xraywrapper.util.ZipFileExtractor.unzipFile;

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
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;

public class XrayWrapperReportReader {

    File cycloneReport;
    File securityReport;
    File sechubReport;

    private HashMap<String, CycloneDXVulnerabilityHelper> cycloneDXVulnerabilityHashMap;

    public void getFiles(String unzippedArchive, String pdsResultFile) throws XrayWrapperReportException {
        sechubReport = new File(pdsResultFile);
        if (!fileExists(unzippedArchive)) {
            // folder with reports is zipped
            String zipArchive = unzippedArchive + ".zip";
            if (fileExists(zipArchive)) {
                unzipFile(Paths.get(zipArchive), Paths.get(unzippedArchive));
            } else {
                throw new XrayWrapperReportException("File with reports does not exist", XrayWrapperExitCode.FILE_NOT_FOUND);
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

    private ArrayList<Path> getFilesByName(String dir, String filename) throws XrayWrapperReportException {
        ArrayList<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(dir), ("*" + filename + "*.json"))) {
            dirStream.forEach(paths::add);
        } catch (IOException e) {
            throw new XrayWrapperReportException("Could not find Security and CycloneDX reports", e, XrayWrapperExitCode.IO_ERROR);
        }
        return paths;
    }

    public void readSecurityReport() throws XrayWrapperReportException {
        XrayWrapperReportParser xrayWrapperReportParser = new XrayWrapperReportParser();
        JsonNode rootNode = xrayWrapperReportParser.getRootDataNode(securityReport);
        cycloneDXVulnerabilityHashMap = xrayWrapperReportParser.transformSecurityReport(rootNode);
    }

    public Bom mapVulnerabilities() throws XrayWrapperReportException {
        return XrayWrapperReportVulnerabilityMapper.mapVulnerabilities(cycloneReport, cycloneDXVulnerabilityHashMap);
    }

    public void writeReport(Bom sbom) throws XrayWrapperReportException {
        XrayWrapperReportWriter.writeReport(sbom, sechubReport);
    }

}