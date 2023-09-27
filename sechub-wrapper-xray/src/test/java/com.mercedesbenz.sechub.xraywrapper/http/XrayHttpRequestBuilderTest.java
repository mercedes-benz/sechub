package com.mercedesbenz.sechub.xraywrapper.http;

import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.mercedesbenz.sechub.xraywrapper.http.XrayHttpRequestBuilder.*;
import static org.junit.jupiter.api.Assertions.*;

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
    public void test_buildGetXrayVersion() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/xray/api/v1/system/version";

        /* execute */
        request = buildGetXrayVersion(url);

        /* test  */
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getBaseUrl());
    }

    @Test
    public void test_generateGetXrayVersion_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> buildGetXrayVersion(null));
    }

    @Test
    public void test_buildCheckArtifactUpload() {
        /* prepare */
        XrayAPIRequest request;
        String apiUrl = "/artifactory/api/storage/myregister/myname/tag/manifest.json";

        /* execute */
        request = buildCheckArtifactUpload(url, artifact, register);

        /* test  */
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getBaseUrl());
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

        /* test  */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getBaseUrl());
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

        /* test  */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getBaseUrl());
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
        String data = "{\"component_name\": \"myname:tag\"," + "\"package_type\": \"docker\"," + "\"sha_256\" : \"sha256\"";

        /* execute */
        request = buildGetScanReports(url, artifact);

        /* test  */
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertEquals(url + apiUrl, request.getBaseUrl());
        assertTrue(request.getData().contains(data));
    }

    @Test
    public void test_buildGetScanReports_null() {
        /* execute + test */
        assertThrows(NullPointerException.class, () -> buildGetScanReports(null, null));
    }
}