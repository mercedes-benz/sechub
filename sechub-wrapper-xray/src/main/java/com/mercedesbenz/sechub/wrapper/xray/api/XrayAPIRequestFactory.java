// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import java.net.MalformedURLException;
import java.net.URL;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;

public class XrayAPIRequestFactory {
    private static final String XRAY_API = "/xray/api/v1";
    private static final String ARTIFACTORY_API = "/artifactory";

    public static XrayAPIRequest createGetXrayVersionRequest(String baseUrl) throws XrayWrapperException {
        String stringUrl = baseUrl + XRAY_API + "/system/version";
        URL url = parseStringToUrl(stringUrl);
        return XrayAPIRequest.Builder.builder().url(url).build();
    }

    public static XrayAPIRequest createCheckArtifactUploadRequest(String baseUrl, XrayWrapperArtifact artifact, String registry) throws XrayWrapperException {
        String stringUrl = baseUrl + ARTIFACTORY_API + "/api/storage/" + registry + "/" + artifact.getName() + "/" + artifact.getTag() + "/manifest.json";
        URL url = parseStringToUrl(stringUrl);
        return XrayAPIRequest.Builder.builder().url(url).authenticationNeeded(true).build();
    }

    public static XrayAPIRequest createScanArtifactRequest(String baseUrl, XrayWrapperArtifact artifact, String registry) throws XrayWrapperException {
        String stringUrl = baseUrl + XRAY_API + "/scanArtifact";
        URL url = parseStringToUrl(stringUrl);
        String data = """
                {"componentID": "%s://%s:%s",\
                "path": "%s/%s/%s/manifest.json"}""".formatted(artifact.getArtifactType().getType(), artifact.getName(), artifact.getTag(), registry,
                artifact.getName(), artifact.getTag());
        return XrayAPIRequest.Builder.builder().url(url).requestMethod(XrayAPIRequest.RequestMethodEnum.POST).authenticationNeeded(true).jsonBody(data).build();
    }

    public static XrayAPIRequest createGetScanStatusRequest(String baseUrl, XrayWrapperArtifact artifact, String registry) throws XrayWrapperException {
        String stringUrl = baseUrl + XRAY_API + "/scan/status/artifact";
        URL url = parseStringToUrl(stringUrl);
        String data = """
                {"path": "%s/%s/%s/manifest.json",\
                 "repository_pkg_type": "%s",\
                 "sha256": "%s"}""".formatted(registry, artifact.getName(), artifact.getTag(), artifact.getArtifactType().getType(), artifact.getChecksum());
        return XrayAPIRequest.Builder.builder().url(url).requestMethod(XrayAPIRequest.RequestMethodEnum.POST).authenticationNeeded(true).jsonBody(data).build();
    }

    public static XrayAPIRequest createGetScanReportsRequest(String baseUrl, XrayWrapperArtifact artifact) throws XrayWrapperException {
        String stringUrl = baseUrl + XRAY_API + "/component/exportDetails";
        URL url = parseStringToUrl(stringUrl);
        String data = """
                {"component_name": "%s:%s",\
                "package_type": "%s",\
                "sha_256": "%s",\
                "violations": true,\
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
                "cyclonedx_format": "json"}""".formatted(artifact.getName(), artifact.getTag(), artifact.getArtifactType().getType(), artifact.getChecksum());
        return XrayAPIRequest.Builder.builder().url(url).requestMethod(XrayAPIRequest.RequestMethodEnum.POST).authenticationNeeded(true).jsonBody(data).build();
    }

    public static XrayAPIRequest createDeleteArtifactRequest(String baseUrl, XrayWrapperArtifact artifact, String registry) throws XrayWrapperException {
        String stringUrl = baseUrl + ARTIFACTORY_API + "/" + registry + "/" + artifact.getName() + "/" + artifact.getTag();
        URL url = parseStringToUrl(stringUrl);
        return XrayAPIRequest.Builder.builder().url(url).requestMethod(XrayAPIRequest.RequestMethodEnum.DELETE).authenticationNeeded(true).build();
    }

    public static XrayAPIRequest createDeleteUploadsRequest(String baseUrl, XrayWrapperArtifact artifact, String registry) throws XrayWrapperException {
        String stringUrl = baseUrl + ARTIFACTORY_API + "/" + registry + "/" + artifact.getName() + "/_uploads";
        URL url = parseStringToUrl(stringUrl);
        return XrayAPIRequest.Builder.builder().url(url).requestMethod(XrayAPIRequest.RequestMethodEnum.DELETE).authenticationNeeded(true).build();
    }

    private static URL parseStringToUrl(String stringUrl) throws XrayWrapperException {
        URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            throw new XrayWrapperException("Could not parse String to URL:" + stringUrl, XrayWrapperExitCode.MALFORMED_URL, e);
        }
        return url;
    }
}
