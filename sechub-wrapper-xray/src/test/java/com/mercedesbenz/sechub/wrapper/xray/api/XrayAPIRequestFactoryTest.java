// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;

class XrayAPIRequestFactoryTest {

    String url;
    String registry;
    XrayWrapperArtifact artifact;

    @BeforeEach
    void beforeEach() {
        url = "http://myurl";
        registry = "registry";
        artifact = new XrayWrapperArtifact("myname", "sha256", "tag", XrayWrapperScanTypes.DOCKER);
    }

    @Test
    void createGetXrayVersionRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        String apiUrl = "/xray/api/v1/system/version";

        /* execute */
        XrayAPIRequest request = XrayAPIRequestFactory.createGetXrayVersionRequest(url);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
    }

    @Test
    void createGetXrayVersionRequest_invalid_url_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> XrayAPIRequestFactory.createGetXrayVersionRequest(invalidUrl));

        /* test */
        assertTrue(exception.getMessage().contains("Could not parse String to URL:invalid url"));
        assertEquals(XrayWrapperExitCode.MALFORMED_URL, exception.getExitCode());
    }

    @Test
    void createCheckArtifactUploadRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        String apiUrl = "/artifactory/api/storage/registry/myname/tag/manifest.json";

        /* execute */
        XrayAPIRequest request = XrayAPIRequestFactory.createCheckArtifactUploadRequest(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
    }

    @Test
    void createCheckArtifactUploadRequest_invalid_url_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class,
                () -> XrayAPIRequestFactory.createCheckArtifactUploadRequest(invalidUrl, artifact, registry));

        /* test */
        assertTrue(exception.getMessage().contains("Could not parse String to URL:invalid url"));
        assertEquals(XrayWrapperExitCode.MALFORMED_URL, exception.getExitCode());
    }

    @Test
    void createScanArtifactRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        String apiUrl = "/xray/api/v1/scanArtifact";
        String data = """
                {"componentID": "docker://myname:tag","path": "registry/myname/tag/manifest.json"}""";

        /* execute */
        XrayAPIRequest request = XrayAPIRequestFactory.createScanArtifactRequest(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
        assertEquals(data, request.getData());
    }

    @Test
    void createScanArtifactRequest_invalid_url_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class,
                () -> XrayAPIRequestFactory.createScanArtifactRequest(invalidUrl, null, null));

        /* test */
        assertTrue(exception.getMessage().contains("Could not parse String to URL:invalid url"));
        assertEquals(XrayWrapperExitCode.MALFORMED_URL, exception.getExitCode());
    }

    @Test
    void createGetScanStatusRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        String apiUrl = "/xray/api/v1/scan/status/artifact";
        String data = "{\"path\": \"registry/myname/tag/manifest.json\", \"repository_pkg_type\": \"docker\", \"sha256\": \"sha256\"}";

        /* execute */
        XrayAPIRequest request = XrayAPIRequestFactory.createGetScanStatusRequest(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
        assertEquals(data, request.getData());
    }

    @Test
    void createGetScanStatusRequest_invalid_url_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class,
                () -> XrayAPIRequestFactory.createGetScanStatusRequest(invalidUrl, artifact, registry));

        /* test */
        assertTrue(exception.getMessage().contains("Could not parse String to URL:invalid url"));
        assertEquals(XrayWrapperExitCode.MALFORMED_URL, exception.getExitCode());
    }

    @Test
    void createGetScanReportsRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        String apiUrl = "/xray/api/v1/component/exportDetails";
        // without java text blocks (this version works)
        String data = "{\"component_name\": \"myname:tag\"," + "\"package_type\": \"docker\"," + "\"sha_256\": \"sha256\"," + "\"violations\": true,"
                + "\"include_ignored_violations\": true," + "\"license\": true," + "\"exclude_unknown\": true," + "\"security\": true,"
                + "\"malicious_code\": true," + "\"iac\": true," + "\"services\": true," + "\"applications\": true," + "\"output_format\": \"json\","
                + "\"spdx\": true," + "\"spdx_format\": \"json\"," + "\"cyclonedx\": true," + "\"cyclonedx_format\": \"json\"}";

        /* execute */
        XrayAPIRequest request = XrayAPIRequestFactory.createGetScanReportsRequest(url, artifact);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
        assertEquals(data, request.getData());
    }

    @Test
    void createGetScanReportsRequest_null_parameter_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class,
                () -> XrayAPIRequestFactory.createGetScanReportsRequest(invalidUrl, artifact));

        /* test */
        assertTrue(exception.getMessage().contains("Could not parse String to URL:invalid url"));
        assertEquals(XrayWrapperExitCode.MALFORMED_URL, exception.getExitCode());
    }

    @Test
    void createDeleteArtifactRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        String stringUrl = "http://myurl/artifactory/" + registry + "/" + artifact.getName() + "/" + artifact.getTag();

        /* execute */
        XrayAPIRequest request = XrayAPIRequestFactory.createDeleteArtifactRequest(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.DELETE, request.getRequestMethodEnum());
        assertEquals(stringUrl, request.getUrl().toString());
    }

    @Test
    void createDeleteArtifactRequest_invalid_url_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class,
                () -> XrayAPIRequestFactory.createDeleteArtifactRequest(invalidUrl, artifact, null));

        /* test */
        assertTrue(exception.getMessage().contains("Could not parse String to URL:invalid url"));
        assertEquals(XrayWrapperExitCode.MALFORMED_URL, exception.getExitCode());
    }

    @Test
    void createDeleteUploadsRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        String stringUrl = "http://myurl/artifactory/" + registry + "/" + artifact.getName() + "/_uploads";

        /* execute */
        XrayAPIRequest request = XrayAPIRequestFactory.createDeleteUploadsRequest(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.DELETE, request.getRequestMethodEnum());
        assertEquals(stringUrl, request.getUrl().toString());
    }

    @Test
    void createDeleteUploadsRequest_invalid_url_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute */
        XrayWrapperException exception = assertThrows(XrayWrapperException.class,
                () -> XrayAPIRequestFactory.createDeleteUploadsRequest(invalidUrl, artifact, null));

        /* test */
        assertTrue(exception.getMessage().contains("Could not parse String to URL:invalid url"));
        assertEquals(XrayWrapperExitCode.MALFORMED_URL, exception.getExitCode());
    }
}