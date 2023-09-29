package com.mercedesbenz.sechub.xraywrapper.reportgenerator;

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

    private File cyclonreport;
    private File securityreport;
    private File sechubReport;
    private HashMap<String, XrayCycloneVulnerability> vulnerabilityHashMap;

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
            cyclonreport = cyclones.get(0).toFile();
            if (pdsResultFile.isEmpty()) {
                String s = cyclonreport.toString().split("\\.")[0];
                sechubReport = new File(s + "-SecHub.json");
            }
        }
        ArrayList<Path> securityPath = getFilesByName(unzippedArchive, "Security");
        if (!securityPath.isEmpty()) {
            securityreport = securityPath.get(0).toFile();
        }
    }

    /**
     * Returns a list of files in the directory which contain the filename
     *
     * @param dir
     * @param filename
     * @return
     */
    private ArrayList<Path> getFilesByName(String dir, String filename) {
        ArrayList<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(dir), ("*" + filename + "*.json"))) {
            dirStream.forEach(path -> paths.add(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return paths;
    }

    /**
     * Ready the security report and creates a CycloneDX vulnerability
     *
     * @throws IOException
     */
    public void readSecurityReport() throws XrayWrapperReportException {
        XrayReportTransformer xrayReportTransformer = new XrayReportTransformer();
        JsonNode rootNode = xrayReportTransformer.getRootDataNode(securityreport);
        vulnerabilityHashMap = xrayReportTransformer.transfromSecurityReport(rootNode);
    }

    public ObjectNode mapVulnerabilities() throws XrayWrapperReportException {
        return XrayVulnerabilityMapper.mapVulnerabilities(cyclonreport, vulnerabilityHashMap);
    }

    public void writeReport(ObjectNode rootNode) throws XrayWrapperReportException {
        XrayReportWriter.writeReport(rootNode, sechubReport);
    }

    public File getCyclonreport() {
        return cyclonreport;
    }

    public File getSecurityreport() {
        return securityreport;
    }

    public File getSechubReport() {
        return sechubReport;
    }
}