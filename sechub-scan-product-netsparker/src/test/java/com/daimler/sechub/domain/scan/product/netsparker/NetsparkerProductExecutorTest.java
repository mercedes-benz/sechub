// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.netsparker;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.netsparker.NetsparkerAdapter;
import com.daimler.sechub.commons.model.SecHubWebScanConfiguration;
import com.daimler.sechub.domain.scan.Target;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.product.config.WithoutProductExecutorConfigInfo;
import com.daimler.sechub.domain.scan.resolve.TargetResolver;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

public class NetsparkerProductExecutorTest {

    private static final URI URI_1_INTERNET = URI.create("https://www.coolstuf1.example.com");
    private static final URI URI_2_INTERNET = URI.create("https://www.coolstuf2.example.com");
    private static final URI URI_3_INTERNET = URI.create("https://www.coolstuf3.example.com");

    private TestNetsparkerProductExecutor executorToTest;
    private SecHubExecutionContext context;
    private NetsparkerAdapter netsparkerAdapter;
    private SecHubConfiguration config;
    private TargetResolver targetResolver;
    private Target target1;
    private Target target2;
    private Target target3;
    private NetsparkerInstallSetup installSetup;
    private ProductExecutorContext executorContext;

    @Before
    public void before() throws Exception {
        context = mock(SecHubExecutionContext.class);
        config = mock(SecHubConfiguration.class);
        executorContext = mock(ProductExecutorContext.class);

        target1 = new Target(URI_1_INTERNET, TargetType.INTERNET);
        target2 = new Target(URI_2_INTERNET, TargetType.INTERNET);
        target3 = new Target(URI_3_INTERNET, TargetType.INTERNET);

        targetResolver = mock(TargetResolver.class);
        when(targetResolver.resolveTarget(URI_1_INTERNET)).thenReturn(target1);
        when(targetResolver.resolveTarget(URI_2_INTERNET)).thenReturn(target2);
        when(targetResolver.resolveTarget(URI_3_INTERNET)).thenReturn(target3);

        netsparkerAdapter = mock(NetsparkerAdapter.class);

        when(context.getConfiguration()).thenReturn(config);
        UUID pseudoJobUUID = UUID.randomUUID();
        when(context.getSechubJobUUID()).thenReturn(pseudoJobUUID);

        when(executorContext.getCurrentProductResult())
                .thenReturn(new ProductResult(pseudoJobUUID, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.NETSPARKER), "resullt1"));

        executorToTest = new TestNetsparkerProductExecutor();

        executorToTest.netsparkerAdapter = netsparkerAdapter;

        installSetup = mock(NetsparkerInstallSetup.class);
        when(installSetup.getBaseURL()).thenReturn("http://www.example.com/netsparker");
        when(installSetup.getUserId()).thenReturn("user");
        when(installSetup.getPassword()).thenReturn("apitoken1");
        when(installSetup.getNetsparkerLicenseId()).thenReturn("license1");

        executorToTest.installSetup = installSetup;
    }

    @Test
    public void when_one_root_url_is_configured_and_adapter_can_handle_2_targets_the_adapter_is_called_1_time() throws Exception {
        /* prepare */
        when(installSetup.isAbleToScan(TargetType.INTERNET)).thenReturn(true);
        when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(false);

        URI uriIntranet = URI_2_INTERNET;// we "reuse" the URI_2 so its used in prepare method but as INTRANET target!
        Target intranetTarget = new Target(uriIntranet, TargetType.INTRANET); // fake this as INTERNET target...
        when(targetResolver.resolveTarget(uriIntranet)).thenReturn(intranetTarget);

        prepareWebScanWithOneInternetURI();

        /* execute */
        executorToTest.execute(context, executorContext);

        /* test */
        verify(netsparkerAdapter, times(1)).start(any(), any());
    }

    @Test
    public void when_one_root_url_is_configured_and_adapter_cannot_handle_target_the_adapter_is_called_0_times() throws Exception {
        /* prepare */
        when(installSetup.isAbleToScan(TargetType.INTERNET)).thenReturn(false);

        prepareWebScanWithOneInternetURI();

        /* execute */
        executorToTest.execute(context, executorContext);

        /* test */
        verify(netsparkerAdapter, never()).start(any(), any());
    }

    private void prepareWebScanWithOneInternetURI() throws URISyntaxException, SecHubExecutionException {
        SecHubWebScanConfiguration webscan = mock(SecHubWebScanConfiguration.class);
        when(config.getWebScan()).thenReturn(Optional.of(webscan));
        URI uri = URI_1_INTERNET;
        when(webscan.getUri()).thenReturn(uri);
    }

    /**
     * Own class to testing purpose - we can mock here the target resolver to
     * protected field
     * 
     * @author Albert Tregnaghi
     *
     */
    private class TestNetsparkerProductExecutor extends NetsparkerProductExecutor {
        private TestNetsparkerProductExecutor() {
            super.targetResolver = NetsparkerProductExecutorTest.this.targetResolver;
        }
    }

}
