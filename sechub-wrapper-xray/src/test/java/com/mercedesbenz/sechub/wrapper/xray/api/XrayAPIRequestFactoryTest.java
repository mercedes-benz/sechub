package com.mercedesbenz.sechub.wrapper.xray.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;

class XrayAPIRequestFactoryTest {

    String url;
    String registry;
    XrayWrapperArtifact artifact;

    @BeforeEach
    void beforeEach() {
        url = "http://myurl";
        registry = "myregister";
        artifact = new XrayWrapperArtifact("myname", "sha256", "tag", XrayWrapperScanTypes.DOCKER);
    }

    @Test
    void createGetXrayVersionRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/system/version";

        /* execute */
        request = XrayAPIRequestFactory.createGetXrayVersionRequest(url);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
    }

    @Test
    void createGetXrayVersionRequest_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> XrayAPIRequestFactory.createGetXrayVersionRequest(invalidUrl));
    }

    @Test
    void createCheckArtifactUploadRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/artifactory/api/storage/myregister/myname/tag/manifest.json";

        /* execute */
        request = XrayAPIRequestFactory.createCheckArtifactUploadRequest(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
    }

    @Test
    void createCheckArtifactUploadRequest_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> XrayAPIRequestFactory.createCheckArtifactUploadRequest(null, null, null));
    }

    @Test
    void createCheckArtifactUploadRequest_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> XrayAPIRequestFactory.createCheckArtifactUploadRequest(invalidUrl, artifact, registry));
    }

    @Test
    void createScanArtifactRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/scanArtifact";
        String data = """
                {"componentID": "docker://myname:tag","path": "myregister/myname/tag/manifest.json"}""";

        /* execute */
        request = XrayAPIRequestFactory.createScanArtifactRequest(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
        assertEquals(data, request.getData());
    }

    @Test
    void createScanArtifactRequest_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> XrayAPIRequestFactory.createScanArtifactRequest(invalidUrl, null, null));
    }

    @Test
    void createGetScanStatusRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/scan/status/artifact";
        String data = "{\"path\": \"myregister/myname/tag/manifest.json\", \"repository_pkg_type\": \"docker\", \"sha256\": \"sha256\"}";

        /* execute */
        request = XrayAPIRequestFactory.createGetScanStatusRequest(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
        assertEquals(data, request.getData());
    }

    @Test
    void createGetScanStatusRequest_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> XrayAPIRequestFactory.createGetScanStatusRequest(null, null, null));
    }

    @Test
    void createGetScanReportsRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/component/exportDetails";
        // without java text blocks (this version works)
        String data = "{\"component_name\": \"myname:tag\"," + "\"package_type\": \"docker\"," + "\"sha_256\": \"sha256\"," + "\"violations\": true,"
                + "\"include_ignored_violations\": true," + "\"license\": true," + "\"exclude_unknown\": true," + "\"security\": true,"
                + "\"malicious_code\": true," + "\"iac\": true," + "\"services\": true," + "\"applications\": true," + "\"output_format\": \"json\","
                + "\"spdx\": true," + "\"spdx_format\": \"json\"," + "\"cyclonedx\": true," + "\"cyclonedx_format\": \"json\"}";

        /* execute */
        request = XrayAPIRequestFactory.createGetScanReportsRequest(url, artifact);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
        assertEquals(data, request.getData());
    }

    @Test
    void createGetScanReportsRequest_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> XrayAPIRequestFactory.createGetScanReportsRequest(null, null));
    }

    @Test
    void createDeleteArtifactRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        XrayAPIRequest request;
        String stringUrl = "http://myurl/artifactory/" + registry + "/" + artifact.getName() + "/" + artifact.getTag();

        /* execute */
        request = XrayAPIRequestFactory.createDeleteArtifactRequest(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.DELETE, request.getRequestMethodEnum());
        assertEquals(stringUrl, request.getUrl().toString());
    }

    @Test
    void createDeleteArtifactRequest_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> XrayAPIRequestFactory.createDeleteArtifactRequest(invalidUrl, artifact, null));
    }

    @Test
    void createDeleteArtifactRequest_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> XrayAPIRequestFactory.createDeleteArtifactRequest(null, null, null));
    }

    @Test
    void createDeleteUploadsRequest_returns_http_request() throws XrayWrapperException {
        /* prepare */
        XrayAPIRequest request;
        String stringUrl = "http://myurl/artifactory/" + registry + "/" + artifact.getName() + "/_uploads";

        /* execute */
        request = XrayAPIRequestFactory.createDeleteUploadsRequest(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.DELETE, request.getRequestMethodEnum());
        assertEquals(stringUrl, request.getUrl().toString());
    }

    @Test
    void createDeleteUploadsRequest_throws_xrayWrapperException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> XrayAPIRequestFactory.createDeleteUploadsRequest(invalidUrl, artifact, null));
    }

    @Test
    void createDeleteUploadsRequest_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> XrayAPIRequestFactory.createDeleteUploadsRequest(null, null, null));
    }
}