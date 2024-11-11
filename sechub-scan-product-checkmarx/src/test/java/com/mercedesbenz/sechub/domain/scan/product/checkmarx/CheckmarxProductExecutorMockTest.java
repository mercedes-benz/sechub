// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.util.StringInputStream;
import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.AdapterExecutionResult;
import com.mercedesbenz.sechub.adapter.AdapterLogId;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxResilienceConsultant;
import com.mercedesbenz.sechub.adapter.mock.MockDataIdentifierFactory;
import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingEntry;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionException;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorCallback;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.mercedesbenz.sechub.domain.scan.resolve.NetworkTargetResolver;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.Profiles;
import com.mercedesbenz.sechub.sharedkernel.security.AbstractSecHubAPISecurityConfiguration;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.mercedesbenz.sechub.sharedkernel.mapping.MappingIdentifier;
import com.mercedesbenz.sechub.sharedkernel.metadata.DefaultMetaDataInspector;
import com.mercedesbenz.sechub.storage.core.JobStorage;

@SuppressWarnings("deprecation")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CheckmarxProductExecutor.class, CheckmarxResilienceConsultant.class,
        CheckmarxProductExecutorMockTest.SimpleTestConfiguration.class, DefaultMetaDataInspector.class })
public class CheckmarxProductExecutorMockTest {

    private static final String PROJECT_EXAMPLE = "projectIdxyz";

    private static final String PATH_EXAMPLE1 = "/somepath/somewhere";

    private static final UUID JOB_UUID = UUID.randomUUID();

    @Autowired
    CheckmarxProductExecutor executorToTest;

    @MockBean
    NetworkTargetResolver targetResolver;

    @MockBean
    MockDataIdentifierFactory mockdataIdentifierFactory;

    @MockBean
    CheckmarxAdapter checkmarxAdapter;

    @MockBean
    CheckmarxInstallSetup installSetup;

    @MockBean
    SecHubStorageService storageService;

    @MockBean
    SystemEnvironmentVariableSupport systemEnvironmentVariableSupport;

    @MockBean
    CodeScanPathCollector codeScanPathCollector;

    @MockBean
    SecHubDirectCheckmarxResilienceConfiguration resilienceConfiguration;

    @Before
    public void before() throws Exception {
        JobStorage storage = Mockito.mock(JobStorage.class);
        when(storage.fetch(any())).thenReturn(new StringInputStream("something as a code..."));
        when(storageService.createJobStorageForProject(any(), any())).thenReturn(storage);

        when(systemEnvironmentVariableSupport.getValueOrVariableContent("user")).thenReturn("checkmarx-user");
        when(systemEnvironmentVariableSupport.getValueOrVariableContent("pwd")).thenReturn("checkmarx-password");
    }

    @Test
    public void action_executor_contains_checkmarx_resilience_consultant_after_postConstruct() {
        assertTrue(executorToTest.fetchResilientExecutor().containsConsultant(CheckmarxResilienceConsultant.class));
    }

    @Test
    public void when_adapter_throws_first_time_adapter_exception_with_exceed_a_retry_is_done() throws Exception {
        /* prepare */
        SecHubExecutionContext context = createExecutionContextForPseudoCodeScan();

        ProductExecutorCallback callback = mock(ProductExecutorCallback.class);
        ProductExecutorConfigSetup setup = createCheckmarxSetupWithAllMandotoryPartsSet();
        ProductExecutorConfig executorConfig = new ProductExecutorConfig(ProductIdentifier.CHECKMARX, 1, setup);

        ProductExecutorContext executorContext = mock(ProductExecutorContext.class);
        when(executorContext.getCallback()).thenReturn(callback);
        when(executorContext.getExecutorConfig()).thenReturn(executorConfig);

        ProductResult currentResult = new ProductResult(JOB_UUID, PROJECT_EXAMPLE, executorConfig, "pseudo-result");
        when(executorContext.getCurrentProductResult()).thenReturn(currentResult);

        /* @formatter:off */
        when(checkmarxAdapter.start(any(),any())).
            thenThrow(new AdapterException(new AdapterLogId("1", "traceId"),"bla bla - Changes exceeded the threshold limit - bla bla")). // first fails
            thenReturn(new AdapterExecutionResult("result2")); // second: access
        /* @formatter:on */

        /* execute */
        executorToTest.execute(context, executorContext);

        /* test */

        verify(checkmarxAdapter, times(2)).start(any(), any()); // the adapter must be called twice
    }

    @Test
    public void when_adapter_throws_two_time_adapter_exception_with_exceed_a_retry_is_done_only_one_time() throws Exception {
        /* prepare */
        SecHubExecutionContext context = createExecutionContextForPseudoCodeScan();

        ProductExecutorCallback callback = mock(ProductExecutorCallback.class);

        ProductExecutorConfigSetup setup = createCheckmarxSetupWithAllMandotoryPartsSet();

        ProductExecutorConfig executorConfig = new ProductExecutorConfig(ProductIdentifier.CHECKMARX, 1, setup);

        ProductExecutorContext executorContext = mock(ProductExecutorContext.class);
        when(executorContext.getCallback()).thenReturn(callback);
        when(executorContext.getExecutorConfig()).thenReturn(executorConfig);

        ProductResult currentResult = new ProductResult(JOB_UUID, PROJECT_EXAMPLE, executorConfig, "pseudo-result");
        when(executorContext.getCurrentProductResult()).thenReturn(currentResult);

        /* @formatter:off */
        when(checkmarxAdapter.start(any(),any())).
            thenThrow(new AdapterException(new AdapterLogId("1", "traceId"),"bla bla - Changes exceeded the threshold limit - bla bla")). // first fails
            thenThrow(new AdapterException(new AdapterLogId("2", "traceId"),"bla bla - Changes exceeded the threshold limit - bla bla")). // second fails
            thenReturn(new AdapterExecutionResult("result2")); // third: would be access but should not happen! resilience shall here only work one time!
        /* @formatter:on */
        SecHubExecutionException expected = null;

        /* execute */
        try {
            executorToTest.execute(context, executorContext);
        } catch (SecHubExecutionException e) {
            expected = e;
        }

        /* test */
        assertNotNull("No SecHubExecutionException happened, but must!", expected);
        verify(checkmarxAdapter, times(2)).start(any(), any()); // the adapter must be called twice - first errr, than one retry, third error
                                                                // means no retry again
        Throwable cause = expected.getCause();
        String message = cause.getMessage();

        assertTrue(message.contains("Changes exceeded the threshold limit"));

    }

    private ProductExecutorConfigSetup createCheckmarxSetupWithAllMandotoryPartsSet() {
        ProductExecutorConfigSetup setup = mock(ProductExecutorConfigSetup.class);
        ProductExecutorConfigSetupCredentials credentials = mock(ProductExecutorConfigSetupCredentials.class);
        when(setup.getCredentials()).thenReturn(credentials);
        when(credentials.getUser()).thenReturn("user");
        when(credentials.getPassword()).thenReturn("pwd");
        List<ProductExecutorConfigSetupJobParameter> jobParameters = new ArrayList<>();
        MappingData data = new MappingData();
        data.getEntries().add(new MappingEntry(".*", "teamId1", ""));

        jobParameters.add(new ProductExecutorConfigSetupJobParameter(MappingIdentifier.CHECKMARX_NEWPROJECT_TEAM_ID.getId(), data.toJSON()));
        when(setup.getJobParameters()).thenReturn(jobParameters);
        return setup;
    }

    private SecHubExecutionContext createExecutionContextForPseudoCodeScan() {
        SecHubExecutionContext context = mock(SecHubExecutionContext.class);
        SecHubConfiguration config = new SecHubConfiguration();
        config.setProjectId(PROJECT_EXAMPLE);
        SecHubCodeScanConfiguration codeScan = new SecHubCodeScanConfiguration();
        SecHubFileSystemConfiguration fileSystemConfig = new SecHubFileSystemConfiguration();
        fileSystemConfig.getFolders().add(PATH_EXAMPLE1);

        codeScan.setFileSystem(fileSystemConfig);
        config.setCodeScan(codeScan);
        when(context.getConfiguration()).thenReturn(config);
        return context;
    }

    @TestConfiguration
    @Profile(Profiles.TEST)
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration extends AbstractSecHubAPISecurityConfiguration {

    }

}
