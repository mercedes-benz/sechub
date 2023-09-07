package com.mercedesbenz.sechub.xraywrapper.cli;

import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIRequest;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayDockerImage;

// This class creates POST and GET request to access the JFrog Artifactory and Xray
public class XrayAPIcalls {

    /**
     * Creates GET request to get the JFrog Xray version
     *
     * @param baseUrl
     * @return XrayAPIRequest
     */
    static XrayAPIRequest getXrayVersion(String baseUrl) {
        XrayAPIRequest request = new XrayAPIRequest();
        request.setBaseUrl(baseUrl + "/xray/api/v1/system/version");
        request.setRequestMethodEnum(XrayAPIRequest.RequestMethodEnum.GET);
        return request;
    }

    /**
     * Creates POST request to see if an artifact was uploaded successful
     *
     * @param baseUrl
     * @param image
     * @param repository
     * @return XrayAPIRequest
     */
    static XrayAPIRequest checkArtifactUpload(String baseUrl, XrayDockerImage image, String repository) {
        String url = baseUrl + "/artifactory/api/storage/" + repository + "/" + image.getDocker_name() + "/" + image.getDocker_tag() + "/manifest.json";
        String data = "";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.GET, true, data);
    }

    /**
     * Creates POST request to scan an artifact with Xray
     *
     * @param baseUrl
     * @param image
     * @param repository
     * @return XrayAPIRequest
     */
    static XrayAPIRequest scanArtifact(String baseUrl, XrayDockerImage image, String repository) {
        String url = baseUrl + "/xray/api/v1/scanArtifact";
        String data = "{\"componentID\": \"docker://" + image.getDocker_name() + ":" + image.getDocker_tag() + "\"," + "\"path\": \"" + repository + "/"
                + image.getDocker_name() + "/" + image.getDocker_tag() + "/manifest.json\"}";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.POST, true, data);
    }

    /**
     * Creates POST request to get the status of an artifact
     *
     * @param baseUrl
     * @param image
     * @param repository
     * @return XrayAPIRequest
     */
    static XrayAPIRequest getScanStatus(String baseUrl, XrayDockerImage image, String repository) {
        String url = baseUrl + "/xray/api/v1/scan/status/artifact";
        String data = "{\"path\": \"" + repository + "/" + image.getDocker_name() + "/" + image.getDocker_tag()
                + "/manifest.json\", \"repository_pkg_type\":\"docker\", \"sha256\": \"" + image.getSHA256() + "\"}";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.POST, true, data);
    }

    /**
     * Creates POST request to download the reports
     *
     * @param baseUrl
     * @param image
     * @param filename
     * @return XrayAPIRequest
     */
    static XrayAPIRequest getScanReports(String baseUrl, XrayDockerImage image, String filename) {
        String url = baseUrl + "/xray/api/v1/component/exportDetails";
        String data = "{\"component_name\": \"" + image.getDocker_name() + ":" + image.getDocker_tag() + "\"," + "\"package_type\": \"docker\","
                + "\"sha_256\" : \"" + image.getSHA256() + "\"," + "\"violations\": true," + "\"include_ignored_violations\": true," + "\"license\": true,"
                + "\"exclude_unknown\": true," + "\"security\": true," + "\"malicious_code\": true," + "\"iac\": true," + "\"services\": true,"
                + "\"applications\": true," + "\"output_format\": \"json\"," + "\"spdx\": true," + "\"spdx_format\": \"json\"," + "\"cyclonedx\": true,"
                + "\"cyclonedx_format\": \"json\"}";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.POST, true, data, filename);
    }
}
