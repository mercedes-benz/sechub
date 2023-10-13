package com.mercedesbenz.sechub.xraywrapper.api;

import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperArtifact;

public class XrayAPIRequestBuilder {

    public static XrayAPIRequest buildGetXrayVersion(String baseUrl) {
        XrayAPIRequest request = new XrayAPIRequest();
        request.setStringUrl(baseUrl + "/xray/api/v1/system/version");
        request.setRequestMethodEnum(XrayAPIRequest.RequestMethodEnum.GET);
        return request;
    }

    public static XrayAPIRequest buildCheckArtifactUpload(String baseUrl, XrayWrapperArtifact artifact, String repository) {
        String url = baseUrl + "/artifactory/api/storage/" + repository + "/" + artifact.getName() + "/" + artifact.getTag() + "/manifest.json";
        String data = "";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.GET, true, data);
    }

    public static XrayAPIRequest buildScanArtifact(String baseUrl, XrayWrapperArtifact artifact, String repository) {
        String url = baseUrl + "/xray/api/v1/scanArtifact";
        String data = "{\"componentID\": \"" + artifact.getArtifactType().getType() + "://" + artifact.getName() + ":" + artifact.getTag() + "\","
                + "\"path\": \"" + repository + "/" + artifact.getName() + "/" + artifact.getTag() + "/manifest.json\"}";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.POST, true, data);
    }

    public static XrayAPIRequest buildGetScanStatus(String baseUrl, XrayWrapperArtifact artifact, String repository) {
        String url = baseUrl + "/xray/api/v1/scan/status/artifact";
        String data = "{\"path\": \"" + repository + "/" + artifact.getName() + "/" + artifact.getTag() + "/manifest.json\", " + "\"repository_pkg_type\":\""
                + artifact.getArtifactType().getType() + "\", \"sha256\": \"" + artifact.getSha256() + "\"}";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.POST, true, data);
    }

    public static XrayAPIRequest buildGetScanReports(String baseUrl, XrayWrapperArtifact artifact) {
        String url = baseUrl + "/xray/api/v1/component/exportDetails";
        String data = """
                {"component_name": \"""" + artifact.getName() + ":" + artifact.getTag() + """
                ","package_type": \"""" + artifact.getArtifactType().getType() + """
                ","sha_256": \"""" + artifact.getSha256() + """
                ","violations": true,\
                "include_ignored_violations": true,\
                "license": true,\
                "exclude_unknown": true,\
                "security": true,\
                "malicious_code": true,\
                "iac": true,\
                "services": true,\
                "applications": true,\
                "output_format": "json",\
                "spdx": true,\
                "spdx_format": "json",\
                "cyclonedx": true,\
                "cyclonedx_format": "json"}""";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.POST, true, data);
    }

    public static XrayAPIRequest buildDeleteArtifact(String baseUrl, XrayWrapperArtifact artifact, String repository) {
        String url = baseUrl + "/artifactory/" + repository + "/" + artifact.getName() + "/" + artifact.getTag();
        String data = "";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.DELETE, true, data);
    }

    public static XrayAPIRequest buildDeleteUploads(String baseUrl, XrayWrapperArtifact artifact, String repository) {
        String url = baseUrl + "/artifactory/" + repository + "/" + artifact.getName() + "/_uploads";
        String data = "";
        return new XrayAPIRequest(url, XrayAPIRequest.RequestMethodEnum.DELETE, true, data);
    }
}
