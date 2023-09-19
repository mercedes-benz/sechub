package com.mercedesbenz.sechub.xraywrapper.reportgenerator;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mercedesbenz.sechub.xraywrapper.util.ReportExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class XrayReportReader {

    private File cyclonreport;
    private File securityreport;
    private File sechubReport;
    HashMap<String, XrayCycloneVulnerability> vulnerabilityHashMap;

    public void readReport(String zipArchiveName, String pdsResultFile) throws IOException {
        sechubReport = new File(pdsResultFile);
        if (!ReportExtractor.fileExists(zipArchiveName)) {
            // folder is zipped :)
            String zipArchive = zipArchiveName + ".zip";
            if (ReportExtractor.fileExists(zipArchive)) {
                ReportExtractor.unzipReports(Paths.get(zipArchive), Paths.get(zipArchiveName));
            } else {
                System.out.println("Error: could not find reports!");
                System.exit(0);
            }
        }

        ArrayList<Path> cyclones = getFilesByName(zipArchiveName, "CycloneDX");
        if (!cyclones.isEmpty()) {
            cyclonreport = cyclones.get(0).toFile();
            if (pdsResultFile.isEmpty()) {
                String s = cyclonreport.toString().split("\\.")[0];
                sechubReport = new File(s + "-SecHub.json");
            }
        }
        ArrayList<Path> securityPath = getFilesByName(zipArchiveName, "Security");
        if (!securityPath.isEmpty()) {
            securityreport = securityPath.get(0).toFile();
        }

        readSecurityReport();
        insertJSON();
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
    private void readSecurityReport() throws IOException {
        vulnerabilityHashMap = new HashMap<String, XrayCycloneVulnerability>();
        final JsonNode rootDataNode = new ObjectMapper().readTree(securityreport).get("data");
        for (JsonNode node : rootDataNode) {
            // create new vulnerability
            XrayCycloneVulnerability vul = new XrayCycloneVulnerability("");
            String cvss_string = "";

            // set bom-ref
            String bom_ref = getBomRef(node);
            if (bom_ref != null)
                vul.setBom_ref(bom_ref);

            // set cve ID and CWEs
            final JsonNode cve_details = new ObjectMapper().readTree(String.valueOf(node)).get("component_versions").get("more_details").get("cves");
            for (JsonNode cve_detail : cve_details) {
                // set cvss string for rating (needed later)
                String cve_id = "default";
                try {
                    cvss_string = cve_detail.get("cvss_v3").asText();
                    cve_id = cve_detail.get("cve").asText();
                    if (cve_id != null)
                        vul.setId(cve_id);
                    ArrayNode cwe = (ArrayNode) cve_detail.get("cwe");
                    if (cwe != null)
                        vul.setCwe(cwe);
                } catch (Exception ignored) {
                }
            }

            // set source
            String name = node.get("provider").asText();
            String url = "";
            if (name != null)
                vul.setSource(url, name);

            // set rating only if cvss_v3 exists, does not care about cvss_v2
            // "cvss_v3": "6.5/CVSS:3.1/AV:N/AC:L/PR:N/UI:R/S:U/C:N/I:H/A:N"
            if (!Objects.equals(cvss_string, "")) {
                String[] cvss_arr = cvss_string.split("/", 3);
                String score = cvss_arr[0];
                String method = cvss_arr[1];
                String vector = cvss_arr[2];
                method = method.replace(":", "v");
                method = method.replace(".", "");
                method = method.replace("0", "");
                String severity = node.get("severity").asText().toLowerCase();
                String rating_source_name = node.get("severity_source").asText().toUpperCase();
                try {
                    vul.setRating(score, severity, method, vector, rating_source_name);
                } catch (Exception ignore) {
                }
            }

            // add description
            try {
                vul.setDescription(node.get("component_versions").get("more_details").get("description").asText());
            } catch (Exception ignore) {
            }

            // add vulnerable versions
            String ref = node.get("source_comp_id").asText();
            String purls = node.get("source_id").asText();
            try {
                vul.addAffects(ref, (ArrayNode) node.get("component_versions").get("vulnerable_versions"),
                        (ArrayNode) node.get("component_versions").get("fixed_versions"), purls);
                vul.putAffects();
            } catch (Exception ignore) {
            }

            // save each vulnerability with ID identifier - possible?
            vulnerabilityHashMap.put(vul.getId(), vul);
        }
    }

    private String getBomRef(JsonNode node) {
        String bom_ref = node.get("id").asText();
        if (bom_ref == null) {
            System.out.println("ERROR: no ID found");
            return null;
        }
        return bom_ref;
    }

    private void insertJSON() throws IOException {
        // todo: What about CVE-ID not in cycloneDX report or same CVE ID?
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        JsonNode rootNode = new ObjectMapper().readTree(cyclonreport);
        JsonNode vulCyclone = rootNode.get("vulnerabilities");
        ArrayNode arryNode = mapper.createArrayNode();

        if (vulCyclone.isArray()) {
            for (JsonNode node : vulCyclone) {
                String id = node.get("id").asText();
                XrayCycloneVulnerability vul = vulnerabilityHashMap.get(id);
                if (vul != null) {
                    JsonNode analysis = node.get("analysis");
                    if (analysis != null) {
                        vul.setAnalysis(analysis);
                    }
                    // rewrite the node
                    arryNode.add(vul.getVulnerability());
                }
            }
        }
        ObjectNode root = (ObjectNode) rootNode;
        root.set("vulnerabilities", arryNode);
        writer.writeValue(new File(sechubReport.toURI()), root);
    }
}