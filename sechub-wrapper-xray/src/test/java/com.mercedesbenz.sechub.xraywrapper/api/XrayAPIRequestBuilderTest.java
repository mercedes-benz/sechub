package com.mercedesbenz.sechub.xraywrapper.api;

import static com.mercedesbenz.sechub.xraywrapper.api.XrayAPIRequestBuilder.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperArtifact;

class XrayAPIRequestBuilderTest {

    String url;
    String register;
    XrayWrapperArtifact artifact;

    @BeforeEach
    public void beforeEach() {
        url = "http://myurl";
        register = "myregister";
        artifact = new XrayWrapperArtifact("myname", "sha256", "tag", "docker");
    }

    @Test
    public void test_buildGetXrayVersion() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/system/version";

        /* execute */
        request = buildGetXrayVersion(url);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getStringUrl());
    }

    @Test
    public void test_generateGetXrayVersion_null() {
        /* execute + test */
        // assertThrows(NullPointerException.class, () -> buildGetXrayVersion(null));
    }

    @Test
    public void test_buildCheckArtifactUpload() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/artifactory/api/storage/myregister/myname/tag/manifest.json";

        /* execute */
        request = buildCheckArtifactUpload(url, artifact, register);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getStringUrl());
    }

    @Test
    public void test_buildCheckArtifactUpload_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> buildCheckArtifactUpload(null, null, null));
    }

    @Test
    public void test_buildScanArtifact() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/scanArtifact";
        String data = "{\"componentID\": \"docker://myname:tag\"," + "\"path\": \"myregister/myname/tag/manifest.json\"}";

        /* execute */
        request = buildScanArtifact(url, artifact, register);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getStringUrl());
        assertEquals(data, request.getData());
    }

    @Test
    public void test_buildScanArtifact_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> buildScanArtifact(null, null, null));
    }

    @Test
    public void test_buildGetScanStatus() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/scan/status/artifact";
        String data = "{\"path\": \"myregister/myname/tag/manifest.json\", \"repository_pkg_type\":\"docker\", \"sha256\": \"sha256\"}";

        /* execute */
        request = buildGetScanStatus(url, artifact, register);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getStringUrl());
        assertEquals(data, request.getData());
    }

    @Test
    public void test_buildGetScanStatus_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> buildGetScanStatus(null, null, null));
    }

    @Test
    public void test_buildGetScanReports() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/component/exportDetails";
        // without java text blocks (this version works)
        String data = "{\"component_name\": \"myname:tag\"," + "\"package_type\": \"docker\"," + "\"sha_256\": \"sha256\"," + "\"violations\": true,"
                + "\"include_ignored_violations\": true," + "\"license\": true," + "\"exclude_unknown\": true," + "\"security\": true,"
                + "\"malicious_code\": true," + "\"iac\": true," + "\"services\": true," + "\"applications\": true," + "\"output_format\": \"json\","
                + "\"spdx\": true," + "\"spdx_format\": \"json\"," + "\"cyclonedx\": true," + "\"cyclonedx_format\": \"json\"}";

        /* execute */
        request = buildGetScanReports(url, artifact);

        /* test */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getStringUrl());
        assertEquals(data, request.getData());
    }

    @Test
    public void test_buildGetScanReports_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> buildGetScanReports(null, null));
    }
}