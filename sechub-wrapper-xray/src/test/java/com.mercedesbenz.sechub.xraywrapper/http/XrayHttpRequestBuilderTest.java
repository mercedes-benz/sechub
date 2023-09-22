package com.mercedesbenz.sechub.xraywrapper.http;

import static com.mercedesbenz.sechub.xraywrapper.http.XrayHttpRequestBuilder.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;

class XrayHttpRequestBuilderTest {

    String url;
    String register;
    XrayArtifact artifact;

    @BeforeEach
    public void beforeEach() {
        url = "http://myurl";
        register = "myregister";
        artifact = new XrayArtifact("myname", "sha256", "tag", "docker");
    }

    @Test
    public void testGenerateGetXrayVersion() {
        // prepare
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/system/version";

        // execute
        request = generateGetXrayVersion(url);

        // assert
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getBaseUrl());
    }

    @Test
    public void testGenerateCheckArtifactUpload() {
        // prepare
        XrayAPIRequest request;
        String apiUrl = "/artifactory/api/storage/myregister/myname/tag/manifest.json";

        // execute
        request = generateCheckArtifactUpload(url, artifact, register);

        // assert
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getBaseUrl());
    }

    @Test
    public void testGenerateScanArtifact() {
        // prepare
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/scanArtifact";
        String data = "{\"componentID\": \"docker://myname:tag\"," + "\"path\": \"myregister/myname/tag/manifest.json\"}";

        // execute
        request = generateScanArtifact(url, artifact, register);

        // assert
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getBaseUrl());
        assertEquals(data, request.getData());
    }

    @Test
    public void testGenerateGetScanStatus() {
        // prepare
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/scan/status/artifact";
        String data = "{\"path\": \"myregister/myname/tag/manifest.json\", \"repository_pkg_type\":\"docker\", \"sha256\": \"sha256\"}";

        // execute
        request = generateGetScanStatus(url, artifact, register);

        // assert
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getBaseUrl());
        assertEquals(data, request.getData());
    }

    @Test
    public void testGenerateGetScanReports() {
        // prepare
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/component/exportDetails";
        String data = "{\"component_name\": \"myname:tag\"," + "\"package_type\": \"docker\"," + "\"sha_256\" : \"sha256\"";

        // execute
        request = generateGetScanReports(url, artifact);

        // assert
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getBaseUrl());
        assertTrue(request.getData().contains(data));
    }
}