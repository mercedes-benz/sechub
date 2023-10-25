package com.mercedesbenz.sechub.wrapper.xray.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperRuntimeException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;

class XrayAPIRequestBuilderTest {

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
    void buildGetXrayVersion_returns_http_request() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/system/version";

        /* execute */
        request = XrayAPIRequestBuilder.buildGetXrayVersion(url);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
    }

    @Test
    void buildGetXrayVersion_throws_xrayRuntimeException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> XrayAPIRequestBuilder.buildGetXrayVersion(invalidUrl));
    }

    @Test
    void buildCheckArtifactUpload_returns_http_request() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/artifactory/api/storage/myregister/myname/tag/manifest.json";

        /* execute */
        request = XrayAPIRequestBuilder.buildCheckArtifactUpload(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
    }

    @Test
    void buildCheckArtifactUpload_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> XrayAPIRequestBuilder.buildCheckArtifactUpload(null, null, null));
    }

    @Test
    void buildCheckArtifactUpload_throws_xrayRuntimeException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> XrayAPIRequestBuilder.buildCheckArtifactUpload(invalidUrl, artifact, registry));
    }

    @Test
    void buildScanArtifact_returns_http_request() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/scanArtifact";
        String data = """
                {"componentID": "docker://myname:tag","path": "myregister/myname/tag/manifest.json"}""";

        /* execute */
        request = XrayAPIRequestBuilder.buildScanArtifact(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
        assertEquals(data, request.getData());
    }

    @Test
    void buildScanArtifact_throws_xrayWrapperRuntimeException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> XrayAPIRequestBuilder.buildScanArtifact(invalidUrl, null, null));
    }

    @Test
    void buildGetScanStatus_returns_http_request() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/scan/status/artifact";
        String data = "{\"path\": \"myregister/myname/tag/manifest.json\", \"repository_pkg_type\":\"docker\", \"sha256\": \"sha256\"}";

        /* execute */
        request = XrayAPIRequestBuilder.buildGetScanStatus(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
        assertEquals(data, request.getData());
    }

    @Test
    void buildGetScanStatus_throws_xrayWrapperRuntimeException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> XrayAPIRequestBuilder.buildGetScanStatus(null, null, null));
    }

    @Test
    void buildGetScanReports_returns_http_request() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/component/exportDetails";
        // without java text blocks (this version works)
        String data = "{\"component_name\": \"myname:tag\"," + "\"package_type\": \"docker\"," + "\"sha_256\": \"sha256\"," + "\"violations\": true,"
                + "\"include_ignored_violations\": true," + "\"license\": true," + "\"exclude_unknown\": true," + "\"security\": true,"
                + "\"malicious_code\": true," + "\"iac\": true," + "\"services\": true," + "\"applications\": true," + "\"output_format\": \"json\","
                + "\"spdx\": true," + "\"spdx_format\": \"json\"," + "\"cyclonedx\": true," + "\"cyclonedx_format\": \"json\"}";

        /* execute */
        request = XrayAPIRequestBuilder.buildGetScanReports(url, artifact);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getUrl().toString());
        assertEquals(data, request.getData());
    }

    @Test
    void buildGetScanReports_throws_xrayWrapperRuntimeException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> XrayAPIRequestBuilder.buildGetScanReports(null, null));
    }

    @Test
    void buildDeleteArtifact_returns_http_request() {
        /* prepare */
        XrayAPIRequest request;
        String stringUrl = "http://myurl/artifactory/" + registry + "/" + artifact.getName() + "/" + artifact.getTag();

        /* execute */
        request = XrayAPIRequestBuilder.buildDeleteArtifact(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.DELETE, request.getRequestMethodEnum());
        assertEquals(stringUrl, request.getUrl().toString());
    }

    @Test
    void buildDeleteArtifact_throws_xrayWrapperRuntimeException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> XrayAPIRequestBuilder.buildDeleteArtifact(invalidUrl, artifact, null));
    }

    @Test
    void buildDeleteArtifact_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> XrayAPIRequestBuilder.buildDeleteArtifact(null, null, null));
    }

    @Test
    void buildDeleteUploads_returns_http_request() {
        /* prepare */
        XrayAPIRequest request;
        String stringUrl = "http://myurl/artifactory/" + registry + "/" + artifact.getName() + "/_uploads";

        /* execute */
        request = XrayAPIRequestBuilder.buildDeleteUploads(url, artifact, registry);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.DELETE, request.getRequestMethodEnum());
        assertEquals(stringUrl, request.getUrl().toString());
    }

    @Test
    void buildDeleteUploads_throws_xrayWrapperRuntimeException() {
        /* prepare */
        String invalidUrl = "invalid url";

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> XrayAPIRequestBuilder.buildDeleteUploads(invalidUrl, artifact, null));
    }

    @Test
    void buildDeleteUploads_throws_nullPointerException() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> XrayAPIRequestBuilder.buildDeleteUploads(null, null, null));
    }
}