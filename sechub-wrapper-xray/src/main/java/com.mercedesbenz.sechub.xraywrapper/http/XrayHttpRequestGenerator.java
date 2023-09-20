package com.mercedesbenz.sechub.xraywrapper.http;

import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIRequest;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayArtifact;

// This class creates POST and GET request to access the JFrog Artifactory and Xray
public class XrayHttpRequestGenerator {

    /**
     * Creates GET request to get the JFrog Xray version
     *
     * @param baseUrl factory URL
     * @return XrayAPIRequest
     */
    public static XrayAPIRequest generateGetXrayVersion(String baseUrl) {
        XrayAPIRequest request = new XrayAPIRequest();
        request.setBaseUrl(baseUrl + "/xray/api/v1/system/version");
        request.setRequestMethodEnum(XrayAPIRequest.RequestMethodEnum.GET);
        return request;
    }

    /**
     * Creates POST request to see if an artifact was uploaded successful
     *
     * @param baseUrl    factory URL
     * @param artifact   Artifact to scan
     * @param repository artifactory repository
     * @return XrayAPIRequest
     */
    public static XrayAPIRequest generateCheckArtifactUpload(String baseUrl, XrayArtifact artifact, String repository) {
        String url = baseUrl + "/artifactory/api/storage/" + repository + "/" + artifact.getName() + "/" + artifact.getTag() + "/manifest.json";
        String data = "";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.GET, true, data);
    }

    /**
     * Creates POST request to scan an artifact with Xray
     *
     * @param baseUrl    factory URL
     * @param artifact   Artifact to scan
     * @param repository artifactory repository
     * @return XrayAPIRequest
     */
    public static XrayAPIRequest generateScanArtifact(String baseUrl, XrayArtifact artifact, String repository) {
        String url = baseUrl + "/xray/api/v1/scanArtifact";
        String data = "{\"componentID\": \"" + artifact.getArtifactType() + "://" + artifact.getName() + ":" + artifact.getTag() + "\"," + "\"path\": \""
                + repository + "/" + artifact.getName() + "/" + artifact.getTag() + "/manifest.json\"}";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.POST, true, data);
    }

    /**
     * Creates POST request to get the status of an artifact
     *
     * @param baseUrl    factory URL
     * @param artifact   Artifact to scan
     * @param repository artifactory repository
     * @return XrayAPIRequest
     */
    public static XrayAPIRequest generateGetScanStatus(String baseUrl, XrayArtifact artifact, String repository) {
        String url = baseUrl + "/xray/api/v1/scan/status/artifact";
        String data = "{\"path\": \"" + repository + "/" + artifact.getName() + "/" + artifact.getTag() + "/manifest.json\", \"repository_pkg_type\":\""
                + artifact.getArtifactType() + "\", \"sha256\": \"" + artifact.getSha256() + "\"}";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.POST, true, data);
    }

    /**
     * Creates POST request to download the reports
     *
     * @param baseUrl  factory URL
     * @param artifact Artifact to scan
     * @return XrayAPIRequest
     */
    public static XrayAPIRequest generateGetScanReports(String baseUrl, XrayArtifact artifact) {
        String url = baseUrl + "/xray/api/v1/component/exportDetails";
        String data = "{\"component_name\": \"" + artifact.getName() + ":" + artifact.getTag() + "\"," + "\"package_type\": \"" + artifact.getArtifactType()
                + "\"," + "\"sha_256\" : \"" + artifact.getSha256() + "\"," + "\"violations\": true," + "\"include_ignored_violations\": true,"
                + "\"license\": true," + "\"exclude_unknown\": true," + "\"security\": true," + "\"malicious_code\": true," + "\"iac\": true,"
                + "\"services\": true," + "\"applications\": true," + "\"output_format\": \"json\"," + "\"spdx\": true," + "\"spdx_format\": \"json\","
                + "\"cyclonedx\": true," + "\"cyclonedx_format\": \"json\"}";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.POST, true, data);
    }
}
