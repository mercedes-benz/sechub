// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.domain.scan.InstallSetup;
import com.daimler.sechub.domain.scan.Target;
import com.daimler.sechub.domain.scan.TargetRegistry.TargetRegistryInfo;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.resolve.TargetResolver;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

public class AbstractInstallSetupProductExecutorTest {
    private static final InetAddress IP_ADRESS1;
    static {
        try {
            IP_ADRESS1 = InetAddress.getByName("192.168.1.1");
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }
    private static final URI URI_1 = URI.create("www.coolstuf1.com");
    private static final URI URI_2 = URI.create("www.coolstuf2.com");
    private static final URI URI_3 = URI.create("www.coolstuf3.com");
    private List<URI> urisForTarget;
    private List<InetAddress> inetAdressesForTarget;

    private TestInstallSetupProductExecutor executorToTest;
    public InstallSetup installSetup;
    private TargetResolver targetResolver;
    private Target target1;
    private Target target2;
    private Target target3;
    private Target target4;
    private SecHubExecutionContext context;
    private ProductExecutorContext executorContext;

    @Before
    public void before() throws Exception {
        urisForTarget = new ArrayList<>();
        inetAdressesForTarget = new ArrayList<>();

        context = mock(SecHubExecutionContext.class);
        executorContext = mock(ProductExecutorContext.class);

        target1 = new Target(URI_1, TargetType.INTERNET);
        target2 = new Target(URI_2, TargetType.INTRANET);
        target3 = new Target(URI_3, TargetType.INTERNET);
        target4 = new Target(IP_ADRESS1, TargetType.INTRANET);

        targetResolver = mock(TargetResolver.class);
        when(targetResolver.resolveTarget(URI_1)).thenReturn(target1);
        when(targetResolver.resolveTarget(URI_2)).thenReturn(target2);
        when(targetResolver.resolveTarget(URI_3)).thenReturn(target3);
        when(targetResolver.resolveTarget(IP_ADRESS1)).thenReturn(target4);

        when(context.getSechubJobUUID()).thenReturn(UUID.randomUUID());

        installSetup = mock(InstallSetup.class);

        executorToTest = new TestInstallSetupProductExecutor();
        executorToTest.targetResolver = targetResolver;
    }

    @Test
    public void code_is_always_scannable() {

    }

    @Test
    public void no_uris_no_ips_defined_even_when_install_setup_says_yes_to_all_no_adapters_called() throws Exception {
        /* prepare */
        when(installSetup.isAbleToScan(TargetType.INTERNET)).thenReturn(true);
        when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(true);

        assertTrue(urisForTarget.isEmpty());
        assertTrue(inetAdressesForTarget.isEmpty());

        /* execute */
        List<ProductResult> result = executorToTest.execute(context, executorContext);

        /* test */
        assertEquals(0, executorToTest.adapterExecutionCallAmount);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void uris_defined_inetadress_empty_when_install_setup_says_yes_the_product_executor_calls_the_adapter_parts_and_returns_data() throws Exception {
        /* prepare */
        when(installSetup.isAbleToScan(TargetType.INTERNET)).thenReturn(true);
        urisForTarget.add(URI_1);
        urisForTarget.add(URI_2);
        urisForTarget.add(URI_3);

        /* execute */
        List<ProductResult> result = executorToTest.execute(context, executorContext);

        /* test */
        assertEquals(1, executorToTest.adapterExecutionCallAmount);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    public void uris_empty_but_inetadress_filled_when_install_setup_says_no_the_product_executor_calls_the_adapter_parts_and_returns_data() throws Exception {
        /* prepare */
        when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(false);
        assertTrue(urisForTarget.isEmpty());
        inetAdressesForTarget.add(IP_ADRESS1);

        /* execute */
        List<ProductResult> result = executorToTest.execute(context, executorContext);

        /* test */
        assertEquals(0, executorToTest.adapterExecutionCallAmount);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void uris_empty_but_inetadress_filled_when_install_setup_says_yes_the_product_executor_calls_the_adapter_parts_and_returns_data() throws Exception {
        /* prepare */
        when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(true);
        assertTrue(urisForTarget.isEmpty());
        inetAdressesForTarget.add(IP_ADRESS1);

        /* execute */
        List<ProductResult> result = executorToTest.execute(context, executorContext);

        /* test */
        assertEquals(1, executorToTest.adapterExecutionCallAmount);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    public void uris_defined_inetadress_empty_when_install_setup_says_yes_for_both_target_types_the_product_executor_calls_the_adapter_parts_and_returns_data()
            throws Exception {
        /* prepare */
        when(installSetup.isAbleToScan(TargetType.INTERNET)).thenReturn(true);
        when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(true);

        urisForTarget.add(URI_1);
        urisForTarget.add(URI_2);
        urisForTarget.add(URI_3);

        /* execute */
        List<ProductResult> result = executorToTest.execute(context, executorContext);

        /* test */
        assertEquals(2, executorToTest.adapterExecutionCallAmount);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    public void uris_defined_inetadress_empty_when_install_setup_says_no_no_adapters_are_executed() throws Exception {
        /* prepare */
        when(installSetup.isAbleToScan(TargetType.INTERNET)).thenReturn(false);
        when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(false);

        urisForTarget.add(URI_1);
        urisForTarget.add(URI_2);
        urisForTarget.add(URI_3);

        /* execute */
        List<ProductResult> result = executorToTest.execute(context, executorContext);

        /* test */
        assertEquals(0, executorToTest.adapterExecutionCallAmount);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private class TestInstallSetupProductExecutor extends AbstractInstallSetupProductExecutor<InstallSetup> {

        private int adapterExecutionCallAmount;

        @Override
        public ProductIdentifier getIdentifier() {
            return ProductIdentifier.CHECKMARX;
        }

        @Override
        protected List<URI> resolveURIsForTarget(SecHubConfiguration config) {
            return urisForTarget;
        }

        @Override
        protected List<InetAddress> resolveInetAdressForTarget(SecHubConfiguration config) {
            return inetAdressesForTarget;
        }

        @Override
        protected List<ProductResult> executeWithAdapter(SecHubExecutionContext context, ProductExecutorContext executorContext, InstallSetup setup,
                TargetRegistryInfo createInfo) throws Exception {
            assertNotNull(createInfo);
            adapterExecutionCallAmount++;
            List<ProductResult> data = new ArrayList<>();
            data.add(new ProductResult());
            return data;
        }

        @Override
        protected InstallSetup getInstallSetup() {
            return AbstractInstallSetupProductExecutorTest.this.installSetup;
        }

        @Override
        protected ScanType getScanType() {
            return null;
        }

        @Override
        public int getVersion() {
            return 4711;
        }
    }

}
