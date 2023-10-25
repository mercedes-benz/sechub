package com.mercedesbenz.sechub.xraywrapper.api;

import java.net.MalformedURLException;
import java.net.URL;

import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;
import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.xraywrapper.report.XrayWrapperReportException;

public class XrayAPIRequestBuilder {

    private static final String xrayAPI = "/xray/api/v1";
    private static final String artifactoryAPI = "/artifactory";

    public static XrayAPIRequest buildGetXrayVersion(String baseUrl) throws XrayWrapperRuntimeException {
        String stringUrl = baseUrl + xrayAPI + "/system/version";
        URL url = parseStringToUrl(stringUrl);
        return XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.GET).build();
    }

    public static XrayAPIRequest buildCheckArtifactUpload(String baseUrl, XrayWrapperArtifact artifact, String repository) throws XrayWrapperRuntimeException {
        String stringUrl = baseUrl + artifactoryAPI + "/api/storage/" + repository + "/" + artifact.getName() + "/" + artifact.getTag() + "/manifest.json";
        URL url = parseStringToUrl(stringUrl);
        return XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.GET).setAuthentication(true).build();
    }

    public static XrayAPIRequest buildScanArtifact(String baseUrl, XrayWrapperArtifact artifact, String repository) throws XrayWrapperReportException {
        String stringUrl = baseUrl + xrayAPI + "/scanArtifact";
        URL url = parseStringToUrl(stringUrl);
        String data = "{\"componentID\": \"" + artifact.getArtifactType().getType() + "://" + artifact.getName() + ":" + artifact.getTag() + "\","
                + "\"path\": \"" + repository + "/" + artifact.getName() + "/" + artifact.getTag() + "/manifest.json\"}";
        return XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.POST).setAuthentication(true).setData(data).build();
    }

    public static XrayAPIRequest buildGetScanStatus(String baseUrl, XrayWrapperArtifact artifact, String repository) throws XrayWrapperRuntimeException {
        String stringUrl = baseUrl + xrayAPI + "/scan/status/artifact";
        URL url = parseStringToUrl(stringUrl);
        String data = "{\"path\": \"" + repository + "/" + artifact.getName() + "/" + artifact.getTag() + "/manifest.json\", " + "\"repository_pkg_type\":\""
                + artifact.getArtifactType().getType() + "\", \"sha256\": \"" + artifact.getChecksum() + "\"}";
        return XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.POST).setAuthentication(true).setData(data).build();
    }

    public static XrayAPIRequest buildGetScanReports(String baseUrl, XrayWrapperArtifact artifact) throws XrayWrapperRuntimeException {
        String stringUrl = baseUrl + xrayAPI + "/component/exportDetails";
        URL url = parseStringToUrl(stringUrl);
        String data = """
                {"component_name": \"""" + artifact.getName() + ":" + artifact.getTag() + """
                ","package_type": \"""" + artifact.getArtifactType().getType() + """
                ","sha_256": \"""" + artifact.getChecksum() + """
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
        return XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.POST).setAuthentication(true).setData(data).build();
    }

    public static XrayAPIRequest buildDeleteArtifact(String baseUrl, XrayWrapperArtifact artifact, String repository) throws XrayWrapperReportException {
        String stringUrl = baseUrl + artifactoryAPI + "/" + repository + "/" + artifact.getName() + "/" + artifact.getTag();
        URL url = parseStringToUrl(stringUrl);
        return XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.DELETE).setAuthentication(true).build();
    }

    public static XrayAPIRequest buildDeleteUploads(String baseUrl, XrayWrapperArtifact artifact, String repository) throws XrayWrapperRuntimeException {
        String stringUrl = baseUrl + artifactoryAPI + "/" + repository + "/" + artifact.getName() + "/_uploads";
        URL url = parseStringToUrl(stringUrl);
        return XrayAPIRequest.Builder.create(url, XrayAPIRequest.RequestMethodEnum.DELETE).setAuthentication(true).build();
    }

    private static URL parseStringToUrl(String stringUrl) {
        URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            throw new XrayWrapperRuntimeException("Could not parse String to URL:" + stringUrl, e, XrayWrapperExitCode.MALFORMED_URL);
        }
        return url;
    }
}
