// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.daimler.sechub.adapter.AdapterException;
import com.daimler.sechub.adapter.AdapterLogId;
import com.daimler.sechub.adapter.checkmarx.CheckmarxAdapter;
import com.daimler.sechub.domain.scan.Target;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.product.ProductExecutorCallback;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.daimler.sechub.domain.scan.resolve.TargetResolver;
import com.daimler.sechub.sharedkernel.Profiles;
import com.daimler.sechub.sharedkernel.SystemEnvironment;
import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubCodeScanConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubFileSystemConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;
import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;
import com.daimler.sechub.sharedkernel.mapping.MappingIdentifier;
import com.daimler.sechub.sharedkernel.metadata.DefaultMetaDataInspector;
import com.daimler.sechub.sharedkernel.storage.StorageService;
import com.daimler.sechub.storage.core.JobStorage;

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
    CheckmarxAdapter checkmarxAdapter;

    @MockBean
    CheckmarxInstallSetup installSetup;

    @MockBean
    StorageService storageService;

    @MockBean
    TargetResolver targetResolver;

    @MockBean
    SystemEnvironment systemEnvironment;

    @Before
    public void before() throws Exception {
        when(installSetup.isAbleToScan(TargetType.CODE_UPLOAD)).thenReturn(true);
        when(targetResolver.resolveTargetForPath(eq(PATH_EXAMPLE1))).thenReturn(new Target("sourcecode...", TargetType.CODE_UPLOAD));
        JobStorage storage = Mockito.mock(JobStorage.class);
        when(storage.fetch(any())).thenReturn(new StringInputStream("something as a code..."));
        when(storageService.getJobStorage(any(), any())).thenReturn(storage);
    }

    @Test
    public void action_executor_contains_checkmarx_resilience_consultant_after_postConstruct() {
        assertTrue(executorToTest.resilientActionExecutor.containsConsultant(CheckmarxResilienceConsultant.class));
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
            thenReturn("result2"); // second: access
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
            thenReturn("result2"); // third: would be access but should not happen! resilience shall here only work one time!
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
        data.getEntries().add(new MappingEntry(".*","teamId1",""));
        
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
    public static class SimpleTestConfiguration extends AbstractAllowSecHubAPISecurityConfiguration {

    }

}
