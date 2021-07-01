// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.daimler.sechub.adapter.AdapterMetaDataCallback;
import com.daimler.sechub.adapter.pds.data.PDSJobStatus.PDSAdapterJobStatusState;
import com.daimler.sechub.test.TestPortProvider;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

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

    private static final int HTTP_PORT = TestPortProvider.DEFAULT_INSTANCE.getWireMockTestHTTPPort();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(HTTP_PORT).httpsPort(HTTPS_PORT));
    private String productIdentifier;
    private UUID sechubJobUUID;
    private Map<String, String> expectedJobParameters;

    @Before
    public void beforeEach() {
        adapterToTest = new PDSAdapterV1();
        callback = mock(AdapterMetaDataCallback.class);

        productIdentifier = "EXAMPLE_PRODUCT";
        sechubJobUUID = UUID.randomUUID();
        expectedJobParameters = new TreeMap<>();
    }

    @Test
    public void when_pds_config_use_sechub_store_not_set__upload_is_called() throws Exception {
        /* @formatter:off */
        expectedJobParameters.put(PDSAdapterConstants.PARAM_KEY_TARGET_TYPE,"");
        
        /* prepare */
        PDSWiremockTestSupport testSupport = PDSWiremockTestSupport.builder(wireMockRule).
                simulateJobCanBeCreated(sechubJobUUID,productIdentifier,expectedJobParameters).
                simulateUploadData("sourcecode.zip").
                simulateMarkReadyToStart().
                simulateFetchJobStatus(PDSAdapterJobStatusState.DONE).
                simulateFetchJobResultOk("testresult").
                build();
        
        testSupport.startPDSServerSimulation();
        
        /* @formatter:on */

        PDSAdapterConfig config = createCodeScanConfiguration(testSupport);

        /* execute */
        adapterToTest.start(config, callback);

        /*
         * test is done by wiremock - every unexpected call or wrong parameters will
         * fail test
         */

    }

    @Test
    public void when_pds_config_use_sechub_store_set_to_false__upload_is_called() throws Exception {
        /* @formatter:off */
        
        /* prepare */
        expectedJobParameters.put(PDSAdapterConstants.PARAM_KEY_TARGET_TYPE,"");
        expectedJobParameters.put(PDSAdapterConstants.PARAM_KEY_USE_SECHUB_STORAGE,"false");
        
        PDSWiremockTestSupport testSupport = PDSWiremockTestSupport.builder(wireMockRule).
                simulateJobCanBeCreated(sechubJobUUID,productIdentifier,expectedJobParameters).
                simulateUploadData("sourcecode.zip").
                simulateMarkReadyToStart().
                simulateFetchJobStatus(PDSAdapterJobStatusState.DONE).
                simulateFetchJobResultOk("testresult").
                build();

        testSupport.startPDSServerSimulation();
        
        PDSAdapterConfig config = createCodeScanConfiguration(testSupport);
        /* @formatter:on */

        /* execute */
        adapterToTest.start(config, callback);

        /*
         * test is done by wiremock - every unexpected call or wrong parameters will
         * fail test
         */
    }

    @Test
    public void when_pds_config_use_sechub_store_set_to_true__upload_is_NOT_called() throws Exception {
        /* @formatter:off */
        
        /* prepare */
        expectedJobParameters.put(PDSAdapterConstants.PARAM_KEY_TARGET_TYPE,"");
        expectedJobParameters.put(PDSAdapterConstants.PARAM_KEY_USE_SECHUB_STORAGE,"true");
        
        
        PDSWiremockTestSupport testSupport = PDSWiremockTestSupport.builder(wireMockRule).
                simulateJobCanBeCreated(sechubJobUUID,productIdentifier,expectedJobParameters).
                //no simulate upload here!
                simulateMarkReadyToStart().
                simulateFetchJobStatus(PDSAdapterJobStatusState.DONE).
                simulateFetchJobResultOk("testresult").
                build();
        
        testSupport.startPDSServerSimulation();
        
        PDSAdapterConfig config = createCodeScanConfiguration(testSupport);
        /* @formatter:on */

        /* execute */
        adapterToTest.start(config, callback);

        /*
         * test is done by wiremock - every unexpected call or wrong parameters will
         * fail test
         */

    }

    /* @formatter:off */
    private PDSAdapterConfig createCodeScanConfiguration(PDSWiremockTestSupport testSupport) {
        String baseURL = testSupport.getTestBaseUrl();
        PDSAdapterConfig config = PDSCodeScanConfigImpl.builder().
                setUser("testuser").
                setTrustAllCertificates(true).
                setPasswordOrAPIToken("examplepwd").
                setProjectId(TEST_PROJECT_ID).
                setPDSProductIdentifier(productIdentifier).
                setProductBaseUrl(baseURL).
                setJobParameters(expectedJobParameters).
                setSecHubJobUUID(sechubJobUUID).
                setSourceCodeZipFileInputStream(new ByteArrayInputStream("test".getBytes())).
                setSourceZipFileChecksum("fakeChecksumForfakeServer").build();
        return config;
    }
    /* @formatter:on */

}
