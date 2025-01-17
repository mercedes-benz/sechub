// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.nessus;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport;
import com.mercedesbenz.sechub.test.junit4.ExpectedExceptionFactory;

public class NessusAdapterV1Test {

    private NessusAdapterV1 adapterToTest;
    private static String NESSUS_7_0_2_GET_POLICIES_RESULT_JSON;

    @BeforeClass
    public static void beforeClass() {
        NESSUS_7_0_2_GET_POLICIES_RESULT_JSON = TestNessusAdapterFileSupport.getTestfileSupport().loadTestFile("nessus_7.0.2.get_policies_result.json");
    }

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();
    private NessusAdapterContext context;
    private NessusAdapterConfig config;

    @Before
    public void before() {
        // System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        adapterToTest = new NessusAdapterV1();

        context = mock(NessusAdapterContext.class);
        config = mock(NessusAdapterConfig.class);
        when(context.getConfig()).thenReturn(config);
        when(context.json()).thenReturn(new JSONAdapterSupport(adapterToTest, context));
    }

    @Test
    public void a_scan_uuid_can_be_extracted() throws Exception {
        /* prepare */
        String data = "{\"scan_uuid\":\"8b0e5c15-7958-0480-28d9-7c5a7069a9f892b6b6880a8600b5\"}";

        /* execute */
        String scan_uuid = adapterToTest.extractScanUUID(context, data);

        /* test */
        assertEquals(scan_uuid, /* NOSONAR */"8b0e5c15-7958-0480-28d9-7c5a7069a9f892b6b6880a8600b5");
    }

    @Test
    public void when_context_has_scanid_123456_the_createLaunchApiURL_returns_host_slash_scans_slash_123456_slash_launch() {
        /* prepare */
        when(context.getNessusScanId()).thenReturn(123456L);
        when(config.getProductBaseURL()).thenReturn("https://localhost:666");

        /* execute */
        String result = adapterToTest.createLaunchApiURL(context);

        /* test */
        assertEquals("https://localhost:666/scans/123456/launch", result);
    }

    @Test
    public void when_context_has_scanid_123456_the_createScanExportApiURL_returns_host_and_123456_export() {
        /* prepare */
        when(context.getNessusScanId()).thenReturn(123456L);
        when(config.getProductBaseURL()).thenReturn("https://localhost:666");

        /* execute */
        String result = adapterToTest.createScanExportApiURL(context);

        /* test */
        assertEquals("https://localhost:666/scans/123456/export", result);
    }

    @Test
    public void when_context_has_scanid_123456_fid_789_the_createScanExportStatusApiURL_returns_host_and_123456_export_789_status() {
        /* prepare */
        when(context.getNessusScanId()).thenReturn(123456L);
        when(config.getProductBaseURL()).thenReturn("https://localhost:666");
        when(context.getExportFileId()).thenReturn("789");

        /* execute */
        String result = adapterToTest.createScanExportStatusApiURL(context);

        /* test */
        assertEquals("https://localhost:666/scans/123456/export/789/status", result);
    }

    @Test
    public void when_context_has_scanid_123456_fid_789_the_createScanExportDownloadApiURL_returns_host_and_123456_export_789_status() {
        /* prepare */
        when(context.getNessusScanId()).thenReturn(123456L);
        when(config.getProductBaseURL()).thenReturn("https://localhost:666");
        when(context.getExportFileId()).thenReturn("789");

        /* execute */
        String result = adapterToTest.createScanExportDownloadApiURL(context);

        /* test */
        assertEquals("https://localhost:666/scans/123456/export/789/download", result);
    }

    @Test
    public void when_context_has_scanid_123456_the_createGetHistoryIsApiURL_returns_host_and_123456() {
        /* prepare */
        when(context.getNessusScanId()).thenReturn(123456L);
        when(config.getProductBaseURL()).thenReturn("https://localhost:666");

        /* execute */
        String result = adapterToTest.createGetHistoryIdsApiURL(context);

        /* test */
        assertEquals("https://localhost:666/scans/123456", result);
    }

    @Test
    public void createScanJSON_creates_validJson() throws Exception {

        /* prepare */

        /* execute */
        String json = adapterToTest.createNewScanJSON(context);

        /* test */
        JSONObject jsonObject = new JSONObject(json);
        assertNotNull(jsonObject);

    }

    @Test
    public void adapter_has_no_api_prefix() throws Exception {
        /* test */
        assertNull(adapterToTest.getAPIPrefix());
    }

    @Test
    public void resolvePolicyUIDByTitle_null_throws_adapterexception() throws Exception {

        /* prepare */
        expected.expect(AdapterException.class);

        /* execute */
        /* test */
        adapterToTest.resolvePolicyUIDByTitle(null, "x", context);

    }

    @Test
    public void resolveHistoryIdByUUID_no_uuid_set_illegal_state_is_thrown() throws Exception {

        /* prepare */
        expected.expect(IllegalStateException.class);
        when(context.getProductContextId()).thenReturn(null);

        /* execute */
        /* test */
        adapterToTest.resolveHistoryIdByUUID("{}", context);

    }

    @Test
    public void resolveHistoryIdByUUID_empty_obj_throws_adapterexception() throws Exception {

        /* prepare */
        expected.expect(AdapterException.class);
        when(context.getProductContextId()).thenReturn("uuid");

        /* execute */
        /* test */
        adapterToTest.resolveHistoryIdByUUID("{}", context);

    }

    @Test
    public void resolveHistoryIdByUUID_non_existing_uuid_throws_adapter_exception() throws Exception {
        /* test */
        expected.expect(AdapterException.class);

        /* prepare */
        String content = "{\"history\":[{\"uuid\":\"my-uuid\", \"history_id\":\"666\"}]}";
        when(context.getProductContextId()).thenReturn("unkown-uuid");

        /* execute */
        adapterToTest.resolveHistoryIdByUUID(content, context);

    }

    @Test
    public void resolveHistoryIdByUUID__existing_uuid_myUUID_returns_history_id_666() throws Exception {
        /* prepare */
        String content = "{\"history\":[{\"uuid\":\"my-uuid\", \"history_id\":\"666\"}]}";
        when(context.getProductContextId()).thenReturn("my-uuid");

        /* execute */
        String result = adapterToTest.resolveHistoryIdByUUID(content, context);

        /* test */
        assertEquals("666", result);
    }

    @Test
    public void resolvePolicyUIDByTitle_empty_throws_adapterexception() throws Exception {

        /* prepare */
        expected.expect(AdapterException.class);

        /* execute */
        /* test */
        adapterToTest.resolvePolicyUIDByTitle("", "x", context);

    }

    @Test
    public void resolvePolicyUIDByTitle_empty_obj_throws_adapterexception() throws Exception {

        /* prepare */
        expected.expect(AdapterException.class);

        /* execute */
        /* test */
        adapterToTest.resolvePolicyUIDByTitle("{}", "x", context);

    }

    @Test
    public void resolvePolicyUIDByTitle_origin_nessus_file_with_advanced_scan_title_returns_uuid() throws Exception {

        /* execute */
        String uuid = adapterToTest.resolvePolicyUIDByTitle(NESSUS_7_0_2_GET_POLICIES_RESULT_JSON, "Advanced Scan", context);

        /* test */
        assertEquals("ad629e16-03b6-8c1d-cef6-ef8c9dd3c658d24bd260ef5f9e66", uuid);

    }

    @Test
    public void resolvePolicyUIDByTitle_origin_nessus_file_with_not_existing_title_returns_null() throws Exception {

        /* execute */
        String uuid = adapterToTest.resolvePolicyUIDByTitle(NESSUS_7_0_2_GET_POLICIES_RESULT_JSON, "Not existing title", context);

        /* test */
        assertNull(uuid);

    }

}