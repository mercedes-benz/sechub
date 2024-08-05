// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaData;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.adapter.support.JSONAdapterSupport;
import com.mercedesbenz.sechub.adapter.support.RestOperationsSupport;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobCreateResult;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatus;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.test.TestUtil;

class PDSAdapterV1Test {

    private static final JSONAdapterSupport JSON = JSONAdapterSupport.FOR_UNKNOWN_ADAPTER;

    private PDSAdapterV1 adapterToTest;
    private PDSAdapterConfig config;
    private AdapterMetaDataCallback callback;
    private UUID sechubJobUUID;
    private RestOperationsSupport restSupport;
    private RestOperations restOperations;
    private UUID pdsJobUUID1;
    private UUID pdsJobUUID2;

    @BeforeEach
    void befoereach() {
        adapterToTest = new PDSAdapterV1();

        sechubJobUUID = UUID.randomUUID();
        pdsJobUUID1 = UUID.randomUUID();
        pdsJobUUID2 = UUID.randomUUID();

        callback = mock(AdapterMetaDataCallback.class);

        config = mock(PDSAdapterConfig.class);

        when(config.getTimeOutInMilliseconds()).thenReturn(30);
        when(config.getTimeToWaitForNextCheckOperationInMilliseconds()).thenReturn(1);

        restSupport = mock(RestOperationsSupport.class);
        restOperations = mock(RestOperations.class);

        adapterToTest.contextFactory = new TestPDSContextFactory();

        preparePDSReportResult(pdsJobUUID1, "productResult1");
        preparePDSReportResult(pdsJobUUID2, "productResult2");
    }

    @Test
    void adapter_config_data_null_throws_illegal_state_exception() throws Exception {
        /* prepare */
        when(config.getPDSAdapterConfigData()).thenReturn(null);

        /* execute */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> adapterToTest.start(config, callback));

        /* test */
        assertEquals("Adapter config data may not be null!", exception.getMessage());
    }

    @Test
    void start__callback_returns_no_metadata__creates_new_pds_job_and_waits_for_result() throws Exception {
        /* prepare */
        prepareMinimumPDSConfig();

        UUID pdsJobUUID = pdsJobUUID1;

        preparePDSJobCreation(pdsJobUUID);
        preparePDSJobStatus(pdsJobUUID, PDSJobStatusState.DONE);
        preparePDSMessages(pdsJobUUID, Collections.emptyList());

        /* execute */
        AdapterExecutionResult result = adapterToTest.start(config, callback);

        /* test */
        assertEquals("productResult1", result.getProductResult());

    }

    @Test
    @DisplayName("Restart - Meta data is found but PDS job uuid is NOT set - creates new PDS job 1")
    void restart_pds_job_uuid_NOT_found_in_metadata() throws Exception {
        /* prepare */
        prepareMinimumPDSConfig();

        UUID pdsJobUUID = pdsJobUUID1;
        AdapterMetaData metaData = new AdapterMetaData();
        when(callback.getMetaDataOrNull()).thenReturn(metaData);

        preparePDSJobCreation(pdsJobUUID);
        preparePDSJobStatus(pdsJobUUID, PDSJobStatusState.DONE);
        preparePDSMessages(pdsJobUUID, Collections.emptyList());

        /* execute */
        AdapterExecutionResult result = adapterToTest.start(config, callback);

        /* test */
        assertEquals("productResult1", result.getProductResult());

    }

    @Test
    @DisplayName("Restart - Meta data found and PDS job uuid set - PDS job in state done- just fetch PDS job 1 results")
    void restart_pds_job_uuid_found_in_metadata_pds_job_done() throws Exception {
        /* prepare */
        prepareMinimumPDSConfig();

        UUID pdsJobUUID = pdsJobUUID1;
        AdapterMetaData metaData = new AdapterMetaData();
        when(callback.getMetaDataOrNull()).thenReturn(metaData);
        metaData.setValue("PDS_JOB_UUID", pdsJobUUID.toString());

        // test no job created: we just prepare no job creation here! If job creation
        // would be called, the test will fail
        preparePDSJobStatus(pdsJobUUID, PDSJobStatusState.DONE);
        preparePDSMessages(pdsJobUUID, Collections.emptyList());

        /* execute */
        AdapterExecutionResult result = adapterToTest.start(config, callback);

        /* test */
        assertEquals("productResult1", result.getProductResult());

    }

    @ParameterizedTest
    @EnumSource(mode = Mode.INCLUDE, value = PDSJobStatusState.class, names = { "RUNNING", "CREATED", "QUEUED", "READY_TO_START" })
    @DisplayName("Restart - Meta data found and PDS job uuid set - but reused PDS job is not in END state - so wait until job1 is in state done")
    void restart_pds_job_uuid_found_in_metadata_pds_job_not_complete_done(PDSJobStatusState formerPdsJobState) throws Exception {
        /* prepare */
        prepareMinimumPDSConfig();

        UUID pdsJobUUID = pdsJobUUID1;
        AdapterMetaData metaData = new AdapterMetaData();
        when(callback.getMetaDataOrNull()).thenReturn(metaData);
        metaData.setValue("PDS_JOB_UUID", pdsJobUUID.toString());

        // test no job created: we just prepare no job creation here! If job creation
        // would be called, the test will fail
        preparePDSJobStatus(pdsJobUUID, formerPdsJobState);
        preparePDSMessages(pdsJobUUID, Collections.emptyList());

        /*
         * simulate job is endless running (we do not wait here, so retires are extreme
         * fast) and then "Even after ... retries" not ended.
         */
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> adapterToTest.start(config, callback));
        TestUtil.assertExceptionMessageStartsWith(exception, "Even after");
        TestUtil.assertExceptionMessageContains(exception, "no job report state acceppted as END was found");

        /*
         * prepare 2 - set now to done, so we can try to restart again. This time it
         * will succeed.
         */
        preparePDSJobStatus(pdsJobUUID, PDSJobStatusState.DONE);

        /* execute */
        AdapterExecutionResult result = adapterToTest.start(config, callback);

        /* test */
        assertEquals("productResult1", result.getProductResult());

    }

    @ParameterizedTest
    @EnumSource(mode = Mode.INCLUDE, value = PDSJobStatusState.class, names = { "CANCELED", "CANCEL_REQUESTED", "FAILED" })
    @DisplayName("Restart - Meta data found and PDS job uuid set - but former job state was not reusable. Must create + use new PDS job 2")
    void restart_pds_job_uuid_found_in_metadata_but_was_canceled(PDSJobStatusState formerPdsJobState) throws Exception {
        /* prepare */
        prepareMinimumPDSConfig();

        UUID formerPdsJobUUID = pdsJobUUID1;
        AdapterMetaData metaData = new AdapterMetaData();
        when(callback.getMetaDataOrNull()).thenReturn(metaData);
        metaData.setValue("PDS_JOB_UUID", formerPdsJobUUID.toString());

        preparePDSJobStatus(formerPdsJobUUID, formerPdsJobState);

        preparePDSJobCreation(pdsJobUUID2);
        preparePDSJobStatus(pdsJobUUID2, PDSJobStatusState.DONE);
        preparePDSMessages(pdsJobUUID2, Collections.emptyList());

        /* execute */
        AdapterExecutionResult result = adapterToTest.start(config, callback);

        /* test */
        assertEquals("productResult2", result.getProductResult());

    }

    @ParameterizedTest
    @EnumSource(value = PDSJobStatusState.class)
    @DisplayName("Cancel - Meta data found and PDS job uuid set and former job state can be canceled")
    void stop_pds_job_uuid_found_in_metadata_and_running(PDSJobStatusState formerPdsJobState) throws Exception {
        /* prepare */
        prepareMinimumPDSConfig();

        UUID formerPdsJobUUID = pdsJobUUID1;
        AdapterMetaData metaData = new AdapterMetaData();
        when(callback.getMetaDataOrNull()).thenReturn(metaData);
        metaData.setValue("PDS_JOB_UUID", formerPdsJobUUID.toString());

        preparePDSJobStatus(formerPdsJobUUID, formerPdsJobState);

        preparePDSJobCreation(pdsJobUUID2);
        preparePDSJobStatus(pdsJobUUID2, PDSJobStatusState.DONE);
        preparePDSMessages(pdsJobUUID2, Collections.emptyList());

        /* execute */
        boolean stopped = adapterToTest.cancel(config, callback);

        /* test */
        assertTrue(stopped);

    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helper.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private void preparePDSJobCreation(UUID pdsJobUUID) throws AdapterException {
        PDSJobCreateResult createResult = new PDSJobCreateResult();
        createResult.jobUUID = pdsJobUUID.toString();
        when(restSupport.postJSON(any(), any())).thenReturn(JSON.toJSON(createResult));
    }

    private void preparePDSJobStatus(UUID pdsJobUUID, PDSJobStatusState state) {
        PDSJobStatus jobStatus = new PDSJobStatus();
        jobStatus.setState(state);
        ResponseEntity<PDSJobStatus> responseEntity = new ResponseEntity<>(jobStatus, HttpStatus.OK);
        when(restOperations.getForEntity(eq("null/api/job/" + pdsJobUUID.toString() + "/status"), eq(PDSJobStatus.class))).thenReturn(responseEntity);
    }

    private void preparePDSReportResult(UUID pdsJobUUID, String result) {
        ResponseEntity<String> responseEntity = new ResponseEntity<>(result, HttpStatus.OK);
        when(restOperations.getForEntity(eq("null/api/job/" + pdsJobUUID.toString() + "/result"), eq(String.class))).thenReturn(responseEntity);
    }

    private void preparePDSMessages(UUID pdsJobUUID, List<SecHubMessage> messages) {
        SecHubMessagesList messagesList = new SecHubMessagesList(messages);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(messagesList.toJSON(), HttpStatus.OK);
        when(restOperations.getForEntity(eq("null/api/job/" + pdsJobUUID.toString() + "/messages"), eq(String.class))).thenReturn(responseEntity);
    }

    private void prepareMinimumPDSConfig() {
        PDSAdapterConfigData data = mock(PDSAdapterConfigData.class);
        when(data.getSecHubJobUUID()).thenReturn(sechubJobUUID);
        when(config.getPDSAdapterConfigData()).thenReturn(data);
    }

    class TestPDSContextFactory implements PDSContextFactory {

        @Override
        public PDSContext create(PDSAdapterConfig config, PDSAdapter pdsAdapter, AdapterRuntimeContext runtimeContext) {

            return new PDSContext(config, pdsAdapter, runtimeContext) {

                public RestOperations getRestOperations() {
                    return restOperations;
                }

                @Override
                public RestOperationsSupport getRestSupport() {
                    return restSupport;
                }
            };
        }

    }

}
