// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterMetaDataCallback;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapterConfig;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxResilienceConfiguration;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxResilienceConsultant;
import com.mercedesbenz.sechub.commons.core.resilience.ResilienceConsultant;
import com.mercedesbenz.sechub.commons.core.resilience.ResilientActionExecutor;
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

        when(environment.getCheckmarxProductBaseURL()).thenReturn("product-base-url1");
        when(environment.getCheckmarxUser()).thenReturn("user1");
        when(environment.getCheckmarxPassword()).thenReturn("checkmarx-pwd1");

        when(context.getProjectId()).thenReturn("project1");
        when(context.getTeamIdForNewProjects()).thenReturn("team1");
        when(context.getPresetIdForNewProjects()).thenReturn(1L);
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
        when(environment.getScanResultCheckPeriodInMilliseconds()).thenReturn(49152);
        when(environment.getScanResultCheckTimeOutInMinutes()).thenReturn(20);

        when(environment.getSecHubJobUUID()).thenReturn("uuid1");
        when(environment.getEngineConfigurationName()).thenReturn("engine1");
        when(environment.getClientSecret()).thenReturn("secret1");

        when(context.createMockDataIdentifier()).thenReturn("folder1;folder2");

        /* execute */
        serviceToTest.startScan();

        ArgumentCaptor<CheckmarxAdapterConfig> adapterConfig = ArgumentCaptor.forClass(CheckmarxAdapterConfig.class);
        ArgumentCaptor<AdapterMetaDataCallback> metaDataCallback = ArgumentCaptor.forClass(AdapterMetaDataCallback.class);

        /* test */
        verify(adapter).start(adapterConfig.capture(), metaDataCallback.capture());

        CheckmarxAdapterConfig config = adapterConfig.getValue();
        assertThat(config.getUser()).isEqualTo("user1");
        assertThat(config.getPasswordOrAPIToken()).isEqualTo("checkmarx-pwd1");

        assertThat(config.isAlwaysFullScanEnabled()).isEqualTo(alwaysFullScan);
        assertThat(config.isTrustAllCertificatesEnabled()).isEqualTo(trustAllCertificates);

        assertThat(config.getTimeOutInMilliseconds()).isEqualTo(20 * 1000 * 60);
        assertThat(config.getTimeToWaitForNextCheckOperationInMilliseconds()).isEqualTo(49152);
        assertThat(config.getProductBaseURL()).isEqualTo("product-base-url1");

        assertThat(config.getSourceCodeZipFileInputStream()).isEqualTo(inputStreamCreatedByContext);

        assertThat(config.getPresetIdForNewProjectsOrNull()).isEqualTo(1L);
        assertThat(config.getTeamIdForNewProjects()).isEqualTo("team1");

        assertThat(config.getProjectId()).isEqualTo("project1");
        assertThat(config.getTraceID()).isEqualTo("uuid1");

        assertThat(config.getEngineConfigurationName()).isEqualTo("engine1");
        assertThat(config.getClientSecret()).isEqualTo("secret1");
        assertThat(config.getMockDataIdentifier()).isEqualTo("folder1;folder2");
    }

    @Test
    void startScan_triggers_resilient_action_executor_and_returns_result() throws Exception {
        /* prepare */
        CheckmarxWrapperScanService spiedServiceToTest = spy(serviceToTest);
        @SuppressWarnings("unchecked")
        ResilientActionExecutor<AdapterExecutionResult> executor = mock(ResilientActionExecutor.class);
        AdapterExecutionResult mockedResult = mock(AdapterExecutionResult.class);

        when(spiedServiceToTest.createResilientActionExecutor()).thenReturn(executor);
        when(executor.executeResilient(any())).thenReturn(mockedResult);

        /* execute */
        AdapterExecutionResult result = spiedServiceToTest.startScan();

        /* test */
        verify(executor).executeResilient(any());
        assertThat(result).isSameAs(mockedResult);
    }

    @Test
    void startScan_created_action_executor_uses_environment_as_config() throws Exception {
        /* prepare */
        CheckmarxWrapperScanService spiedServiceToTest = spy(serviceToTest);
        @SuppressWarnings("unchecked")
        ResilientActionExecutor<AdapterExecutionResult> executor = mock(ResilientActionExecutor.class);

        when(spiedServiceToTest.createResilientActionExecutor()).thenReturn(executor);

        /* execute */
        spiedServiceToTest.startScan();

        /* test */
        ArgumentCaptor<ResilienceConsultant> captor = ArgumentCaptor.forClass(ResilienceConsultant.class);
        verify(executor).add(captor.capture());

        List<ResilienceConsultant> values = captor.getAllValues();
        assertThat(values).hasSize(1);
        ResilienceConsultant consultant = values.iterator().next();
        assertThat(consultant).isInstanceOf(CheckmarxResilienceConsultant.class);

        CheckmarxResilienceConsultant checkmarxResilienceConsultant = (CheckmarxResilienceConsultant) consultant;
        CheckmarxResilienceConfiguration config = checkmarxResilienceConsultant.getResilienceConfig();

        assertThat(config).isSameAs(environment);
    }
}