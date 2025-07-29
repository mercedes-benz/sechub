// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static com.mercedesbenz.sechub.test.TestConstants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.pds.PDSCodeScanConfigImpl.PDSCodeScanConfigBuilder;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubMessagesList;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.test.TestPortProvider;

/**
 * Junit 4 test because of missing official WireMock Junit5 extension - so we
 * use WireMock Rule and Junit4.
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSAdapterV1WireMockTest {

    private static final String TEST_PROJECT_ID = "testproject1";
    private PDSAdapterV1 adapterToTest;
    private AdapterMetaDataCallback callback;

    private static final int HTTPS_PORT = TestPortProvider.DEFAULT_INSTANCE.getWireMockTestHTTPSPort();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().httpsPort(HTTPS_PORT));

    private String productIdentifier;
    private UUID sechubJobUUID;
    private Map<String, String> expectedJobParameters;

    private static final long SIM_SOURCE_ZIP_SIZE = 1234567;
    private static final long SIM_BINARIES_TAR_SIZE = 4711;

    @Before
    public void beforeEach() {
        adapterToTest = new PDSAdapterV1();
        adapterToTest.contextFactory = new PDSContextFactoryImpl();

        callback = mock(AdapterMetaDataCallback.class);

        productIdentifier = "EXAMPLE_PRODUCT";
        sechubJobUUID = UUID.randomUUID();
        expectedJobParameters = new TreeMap<>();
    }

    @Test
    public void when_pds_config_use_sechub_store_not_set__upload_is_called() throws Exception {
        /* @formatter:off */
        expectedJobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_TYPE,"");

        /* prepare */
        TestPDSWiremockSupport testSupport = TestPDSWiremockSupport.builder(wireMockRule).
                simulateJobCanBeCreated(sechubJobUUID,productIdentifier,expectedJobParameters).
                simulateUploadData(SOURCECODE_ZIP, SIM_SOURCE_ZIP_SIZE).
                simulateMarkReadyToStart().
                simulateFetchJobStatus(PDSJobStatusState.DONE).
                simulateFetchJobResultOk("testresult").
                simulateFetchJobMessages().
                build();

        testSupport.startPDSServerSimulation();

        /* @formatter:on */
        PDSAdapterConfig config = createCodeScanConfiguration(testSupport);

        /* execute */
        adapterToTest.start(config, callback);

        /* test */
        testSupport.verfifyExpectedCalls();
    }

    @Test
    public void when_pds_config_use_sechub_store_set_to_false__upload_is_called() throws Exception {
        /* @formatter:off */

        /* prepare */
        expectedJobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_TYPE,"");
        expectedJobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE,"false");

        TestPDSWiremockSupport testSupport = TestPDSWiremockSupport.builder(wireMockRule).
                simulateJobCanBeCreated(sechubJobUUID,productIdentifier,expectedJobParameters).
                simulateUploadData(SOURCECODE_ZIP,SIM_SOURCE_ZIP_SIZE).
                simulateMarkReadyToStart().
                simulateFetchJobStatus(PDSJobStatusState.DONE).
                simulateFetchJobResultOk("testresult").
                simulateFetchJobMessages().
                build();

        testSupport.startPDSServerSimulation();

        PDSAdapterConfig config = createCodeScanConfiguration(testSupport);
        /* @formatter:on */

        /* execute */
        adapterToTest.start(config, callback);

        /* test */
        testSupport.verfifyExpectedCalls();
    }

    @Test
    public void when_pds_config_use_sechub_store_set_to_false__upload_is_called__binary_variant() throws Exception {
        /* @formatter:off */

        /* prepare */
        expectedJobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_TYPE,"");
        expectedJobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE,"false");

        TestPDSWiremockSupport testSupport = TestPDSWiremockSupport.builder(wireMockRule).
                simulateJobCanBeCreated(sechubJobUUID,productIdentifier,expectedJobParameters).
                simulateUploadData(BINARIES_TAR,SIM_BINARIES_TAR_SIZE).
                simulateMarkReadyToStart().
                simulateFetchJobStatus(PDSJobStatusState.DONE).
                simulateFetchJobResultOk("testresult").
                simulateFetchJobMessages().
                build();

        testSupport.startPDSServerSimulation();

        PDSAdapterConfig config = createCodeScanConfigurationWithBinary(testSupport);
        /* @formatter:on */

        /* execute */
        adapterToTest.start(config, callback);

        /* test */
        testSupport.verfifyExpectedCalls();
    }

    @Test
    public void when_pds_config_use_sechub_store_set_to_true__upload_is_NOT_called() throws Exception {
        /* @formatter:off */

        /* prepare */
        expectedJobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_TYPE,"");
        expectedJobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE,"true");


        TestPDSWiremockSupport testSupport = TestPDSWiremockSupport.builder(wireMockRule).
                simulateJobCanBeCreated(sechubJobUUID,productIdentifier,expectedJobParameters).
                //no simulate upload here! --> if an upload would be called, wiremock would fail, because no stubbing available
                simulateMarkReadyToStart().
                simulateFetchJobStatus(PDSJobStatusState.DONE).
                simulateFetchJobResultOk("testresult").
                simulateFetchJobMessages().
                build();

        testSupport.startPDSServerSimulation();

        PDSAdapterConfig config = createCodeScanConfiguration(testSupport);
        /* @formatter:on */

        /* execute */
        adapterToTest.start(config, callback);

        /* test */
        testSupport.verfifyExpectedCalls();

    }

    @Test
    public void messages_are_returned_to_adapter_result() throws Exception {
        /* @formatter:off */

        /* prepare */
        expectedJobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_TARGET_TYPE,"");
        expectedJobParameters.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE,"true");

        SecHubMessagesList messagesFromPDS = new SecHubMessagesList();
        messagesFromPDS.getSecHubMessages().add(new SecHubMessage(SecHubMessageType.INFO,"i am the info sent back by wiremock"));


        TestPDSWiremockSupport testSupport = TestPDSWiremockSupport.builder(wireMockRule).
                simulateJobCanBeCreated(sechubJobUUID,productIdentifier,expectedJobParameters).
                //no simulate upload here!
                simulateMarkReadyToStart().
                simulateFetchJobStatus(PDSJobStatusState.DONE).
                simulateFetchJobResultOk("testresult").
                simulateFetchJobMessages(messagesFromPDS).
                build();

        testSupport.startPDSServerSimulation();

        PDSAdapterConfig config = createCodeScanConfiguration(testSupport);
        /* @formatter:on */

        /* execute */
        AdapterExecutionResult result = adapterToTest.start(config, callback);

        /* test */
        testSupport.verfifyExpectedCalls();
        assertEquals(Arrays.asList(new SecHubMessage(SecHubMessageType.INFO, "i am the info sent back by wiremock")), result.getProductMessages());
    }

    /* @formatter:off */
    private PDSAdapterConfig createCodeScanConfiguration(TestPDSWiremockSupport testSupport) {
        String baseURL = testSupport.getTestBaseUrl();
        PDSCodeScanConfigBuilder builder = PDSCodeScanConfigImpl.builder().
                setUser("testuser").
                setTrustAllCertificates(true).
                setPasswordOrAPIToken("examplepwd").
                setProjectId(TEST_PROJECT_ID).
                setProductBaseUrl(baseURL);

        PDSAdapterConfigurator configurator = builder.getPDSAdapterConfigurator();
        configurator.setPdsProductIdentifier(productIdentifier);
        configurator.setJobParameters(expectedJobParameters);
        configurator.setSecHubJobUUID(sechubJobUUID);

            configurator.setSourceCodeZipFileInputStreamOrNull(new ByteArrayInputStream("test".getBytes()));
            configurator.setSourceCodeZipFileChecksumOrNull("fakeChecksumForfakeServer");
            configurator.setSourceCodeZipFileRequired(true);
            configurator.setSourceCodeZipFileSizeInBytes(SIM_SOURCE_ZIP_SIZE);

        configurator.setReusingSecHubStorage(testSupport.useSecHubStorage);
        configurator.setScanType(ScanType.CODE_SCAN);
        configurator.setBinaryTarFileRequired(false);

        PDSAdapterConfig config = builder.build();

        return config;
    }
    /* @formatter:on */

    /* @formatter:off */
    private PDSAdapterConfig createCodeScanConfigurationWithBinary(TestPDSWiremockSupport testSupport) {
        String baseURL = testSupport.getTestBaseUrl();
        PDSCodeScanConfigBuilder builder = PDSCodeScanConfigImpl.builder().
                setUser("testuser").
                setTrustAllCertificates(true).
                setPasswordOrAPIToken("examplepwd").
                setProjectId(TEST_PROJECT_ID).
                setProductBaseUrl(baseURL);

        PDSAdapterConfigurator configurator = builder.getPDSAdapterConfigurator();
        configurator.setPdsProductIdentifier(productIdentifier);
        configurator.setJobParameters(expectedJobParameters);
        configurator.setSecHubJobUUID(sechubJobUUID);

        if (! testSupport.useSecHubStorage) {
            configurator.setBinaryTarFileInputStreamOrNull(new ByteArrayInputStream("test".getBytes()));
            configurator.setBinariesTarFileChecksumOrNull("fakeChecksumForfakeServer");
            configurator.setBinaryTarFileRequired(true);
            configurator.setBinariesTarFileSizeInBytes(SIM_BINARIES_TAR_SIZE);
        }
        configurator.setSourceCodeZipFileRequired(false);

        configurator.setReusingSecHubStorage(testSupport.useSecHubStorage);
        configurator.setScanType(ScanType.CODE_SCAN);

        PDSAdapterConfig config = builder.build();

        return config;
    }
    /* @formatter:on */

}
