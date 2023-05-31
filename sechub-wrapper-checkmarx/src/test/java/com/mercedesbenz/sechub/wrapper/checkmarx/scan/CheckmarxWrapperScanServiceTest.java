// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.test.TestUtil;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperEnvironment;

class CheckmarxWrapperScanServiceTest {

    private CheckmarxWrapperScanService serviceToTest;
    private CheckmarxAdapter adapter;
    private CheckmarxWrapperEnvironment environment;
    private CheckmarxWrapperScanContextFactory factory;
    private CheckmarxWrapperScanContext context;
    private InputStream inputStreamCreatedByContext;

    @BeforeEach
    void beforeEach() throws Exception {

        adapter = mock(CheckmarxAdapter.class);
        environment = mock(CheckmarxWrapperEnvironment.class);
        factory = mock(CheckmarxWrapperScanContextFactory.class);
        context = mock(CheckmarxWrapperScanContext.class);

        inputStreamCreatedByContext = mock(InputStream.class);
        when(context.createSourceCodeZipFileInputStream()).thenReturn(inputStreamCreatedByContext);

        Path messagesFolder = TestUtil.createTempDirectoryInBuildFolder("pds-user-messages-folder");
        when(environment.getPdsUserMessagesFolder()).thenReturn(messagesFolder.toString());

        when(factory.create(environment)).thenReturn(context);

        serviceToTest = new CheckmarxWrapperScanService();
        serviceToTest.adapter = adapter;
        serviceToTest.environment = environment;
        serviceToTest.factory = factory;

    }

    /* @formatter:off */
    @ParameterizedTest
    @CsvSource({
        "true,false",
        "true,true",
        "false,false",
        "false,true"})
    /* @formatter:on */
    void adapter_is_used_with_correct_configuration(boolean alwaysFullScan, boolean trustAllCertificates) throws Exception {
        /* prepare */
        AdapterExecutionResult result = new AdapterExecutionResult("something");
        when(adapter.start(any(), any())).thenReturn(result);

        when(environment.isAlwaysFullScanEnabled()).thenReturn(alwaysFullScan);
        when(environment.isTrustAllCertificatesEnabled()).thenReturn(trustAllCertificates);
        when(environment.getScanResultCheckPeriodInMilliseconds()).thenReturn(10);
        when(environment.getScanResultCheckTimeOutInMinutes()).thenReturn(20);

        when(environment.getCheckmarxProductBaseURL()).thenReturn("product-base-url1");
        when(environment.getCheckmarxUser()).thenReturn("user1");
        when(environment.getCheckmarxPassword()).thenReturn("checkmarx-pwd1");
        when((environment.getSecHubJobUUID())).thenReturn("uuid1");
        when(environment.getEngineConfigurationName()).thenReturn("engine1");
        when(environment.getClientSecret()).thenReturn("secret1");

        when(context.getProjectId()).thenReturn("project1");
        when(context.getTeamIdForNewProjects()).thenReturn("team1");
        when(context.getPresetIdForNewProjects()).thenReturn(1L);

        when(context.createMockDataIdentifier()).thenReturn("folder1;folder2");

        /* execute */
        serviceToTest.startScan();

        ArgumentCaptor<CheckmarxAdapterConfig> adapterConfig = ArgumentCaptor.forClass(CheckmarxAdapterConfig.class);
        ArgumentCaptor<AdapterMetaDataCallback> metaDataCallack = ArgumentCaptor.forClass(AdapterMetaDataCallback.class);

        /* test */
        verify(adapter).start(adapterConfig.capture(), metaDataCallack.capture());

        CheckmarxAdapterConfig config = adapterConfig.getValue();
        assertEquals("user1", config.getUser());
        assertEquals("checkmarx-pwd1", config.getPasswordOrAPIToken());

        assertEquals(alwaysFullScan, config.isAlwaysFullScanEnabled());
        assertEquals(trustAllCertificates, config.isTrustAllCertificatesEnabled());

        assertEquals(20 * 1000 * 60, config.getTimeOutInMilliseconds());
        assertEquals(10 * 1000 * 60, config.getTimeToWaitForNextCheckOperationInMilliseconds());
        assertEquals("product-base-url1", config.getProductBaseURL());

        assertEquals(inputStreamCreatedByContext, config.getSourceCodeZipFileInputStream());

        assertEquals(1L, config.getPresetIdForNewProjectsOrNull());
        assertEquals("team1", config.getTeamIdForNewProjects());

        assertEquals("project1", config.getProjectId());
        assertEquals("uuid1", config.getTraceID());

        assertEquals("engine1", config.getEngineConfigurationName());
        assertEquals("secret1", config.getClientSecret());
        assertEquals("folder1;folder2", config.getMockDataIdentifier());
    }

}
