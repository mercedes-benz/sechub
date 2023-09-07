package com.mercedesbenz.sechub.xraywrapper.cli;

import java.io.*;

import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIRequest;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIResponse;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayDockerImage;
import com.mercedesbenz.sechub.xraywrapper.reportgenerator.XrayReportReader;

public class XrayClientArtifactoryManager {
    private String baseUrl;

    private String repository;

    private XrayDockerImage image;

    private String reportfiles;

    public XrayClientArtifactoryManager(String baseUrl, String repository, XrayDockerImage image, String reportfiles) {
        this.baseUrl = baseUrl;
        this.repository = repository;
        this.image = image;
        this.reportfiles = reportfiles;
    }

    public void start() throws IOException {
        // performs all necessary API calls to get reports
        // program exists, when Error code occurs
        // todo: correct error handling
        XrayAPIRequest request;
        XrayAPIResponse response;

        request = XrayAPIcalls.getXrayVersion(baseUrl);
        response = request.sendRequest();
        checkResponse(response);

        request = XrayAPIcalls.checkArtifactUpload(baseUrl, image, repository);
        response = request.sendRequest();
        checkResponse(response);

        request = XrayAPIcalls.scanArtifact(baseUrl, image, repository);
        response = request.sendRequest();
        checkResponse(response);

        request = XrayAPIcalls.getScanStatus(baseUrl, image, repository);
        response = request.sendRequest();
        checkResponse(response);

        request = XrayAPIcalls.getScanReports(baseUrl, image, reportfiles);
        response = request.sendRequest();
        checkResponse(response);

        manageReports();
    }

    private void checkResponse(XrayAPIResponse response) {
        response.print();
        if (response.getStatus_code() > 299) {
            System.out.println("Error Response:");
            System.out.println(response.getBody());
            System.exit(0);
        }
    }

    private void manageReports() {
        // hardcoded in response builder
        String filename = reportfiles;
        XrayReportReader reportReader = new XrayReportReader();
        try {
            reportReader.readReport(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
