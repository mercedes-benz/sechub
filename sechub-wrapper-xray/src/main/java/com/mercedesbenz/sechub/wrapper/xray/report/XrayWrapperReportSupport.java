// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cyclonedx.model.Bom;

import com.fasterxml.jackson.databind.JsonNode;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperJSONConverter;
import com.mercedesbenz.sechub.wrapper.xray.util.ZipFileExtractor;

public class XrayWrapperReportSupport {

    public record XrayReportFiles(File xrayPdsReport, File cycloneReport, File securityReport) {
    }

    public XrayReportFiles collectXrayReportsInArchive(String unzippedArchive, String pdsResultFile) throws XrayWrapperReportException {
        File xrayPdsReport = new File(pdsResultFile);
        ZipFileExtractor zipFileExtractor = new ZipFileExtractor();
        File cycloneReport;
        File securityReport;

        if (!zipFileExtractor.fileExists(unzippedArchive)) {
            // folder with reports is zipped
            String zipArchive = unzippedArchive + ".zip";
            if (zipFileExtractor.fileExists(zipArchive)) {
                zipFileExtractor.unzipFile(Paths.get(zipArchive), Paths.get(unzippedArchive));
            } else {
                throw new XrayWrapperReportException("File with reports does not exist");
            }
        }

        List<Path> cycloneDXReportPaths = getFilesByName(unzippedArchive, "CycloneDX");
        List<Path> securityReportsPaths = getFilesByName(unzippedArchive, "Security");
        if (cycloneDXReportPaths.isEmpty() || securityReportsPaths.isEmpty()) {
            throw new XrayWrapperReportException("Report path cannot be empty, Could not find Xray reports for CycloneDX and Security report");
        }
        securityReport = securityReportsPaths.get(0).toFile();
        cycloneReport = cycloneDXReportPaths.get(0).toFile();

        // if no report was set for the generated report a new filename is created
        if (pdsResultFile.isEmpty()) {
            String fileName = cycloneReport.toString().split("\\.")[0];
            xrayPdsReport = new File(fileName + "-SecHub.json");
        }

        return new XrayReportFiles(xrayPdsReport, cycloneReport, securityReport);
    }

    public Map readSecurityReport(File securityReport) throws XrayWrapperException {
        XrayWrapperReportTransformer transformer = new XrayWrapperReportTransformer();
        JsonNode rootNode = resolveRootDataNode(securityReport);
        return transformer.transformVulnerabilitiesFromSecurityReport(rootNode);
    }

    public Bom mapVulnerabilities(File cycloneReport, Map cycloneDXVulnerabilityMap) throws XrayWrapperReportException {
        XrayWrapperReportVulnerabilityMapper vulnerabilityMapper = new XrayWrapperReportVulnerabilityMapper();
        return vulnerabilityMapper.mapVulnerabilities(cycloneReport, cycloneDXVulnerabilityMap);
    }

    public void writeReport(Bom sbom, File xrayPdsReport) throws XrayWrapperReportException {
        XrayWrapperReportWriter reportWriter = new XrayWrapperReportWriter();
        reportWriter.writeReport(sbom, xrayPdsReport);
    }

    private List<Path> getFilesByName(String dir, String filename) throws XrayWrapperReportException {
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(dir), ("*" + filename + "*.json"))) {
            dirStream.forEach(paths::add);
        } catch (IOException e) {
            throw new XrayWrapperCreateCycloneDXReportException("Could not find Security and CycloneDX reports", e);
        }
        return paths;
    }

    private JsonNode resolveRootDataNode(File xraySecurityReport) throws XrayWrapperException {
        return XrayWrapperJSONConverter.get().readJSONFromFile(xraySecurityReport).get("data");
    }
}