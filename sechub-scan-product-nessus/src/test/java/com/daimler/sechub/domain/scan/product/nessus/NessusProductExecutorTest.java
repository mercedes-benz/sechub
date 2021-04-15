// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.nessus;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.adapter.nessus.NessusAdapter;
import com.daimler.sechub.domain.scan.Target;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.product.ProductExecutorContext;
import com.daimler.sechub.domain.scan.product.ProductIdentifier;
import com.daimler.sechub.domain.scan.product.ProductResult;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.resolve.TargetResolver;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubInfrastructureScanConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionException;

public class NessusProductExecutorTest {
	
	private static final URI URI_1 = URI.create("www.coolstuf1.com");
	private static final URI URI_2 = URI.create("www.coolstuf2.com");
	private static final URI URI_3 = URI.create("www.coolstuf3.com");

	private NessusProductExecutor executorToTest;
	private SecHubExecutionContext context;
	private NessusAdapter nessusAdapter;
	private SecHubConfiguration config;
	private TargetResolver targetResolver;
	private Target target1;
	private Target target2;
	private Target target3;
	private NessusInstallSetup installSetup;
    private ProductExecutorContext executorContext;
    private ProductExecutorConfig executorConfig;

	@Before
	public void before() throws Exception {
		context = mock(SecHubExecutionContext.class);
		config = mock(SecHubConfiguration.class);

		target1 = new Target(URI_1, TargetType.INTERNET);
		target2 = new Target(URI_2, TargetType.INTERNET);
		target3 = new Target(URI_3, TargetType.INTERNET);

		targetResolver = mock(TargetResolver.class);
		when(targetResolver.resolveTarget(URI_1)).thenReturn(target1);
		when(targetResolver.resolveTarget(URI_2)).thenReturn(target2);
		when(targetResolver.resolveTarget(URI_3)).thenReturn(target3);

		nessusAdapter = mock(NessusAdapter.class);
		executorContext=mock(ProductExecutorContext.class);
		executorConfig=mock(ProductExecutorConfig.class);
	
		ProductResult productResult = mock(ProductResult.class);
		when(executorContext.getExecutorConfig()).thenReturn(executorConfig);
		when(executorContext.getCurrentProductResult()).thenReturn(productResult);
		
		installSetup= mock(NessusInstallSetup.class);
		when(installSetup.getBaseURL(any())).thenReturn("baseURL");
		when(installSetup.getUserId(any())).thenReturn("user");
		when(installSetup.getPassword(any())).thenReturn("password");
		
		when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(false);

		when(context.getConfiguration()).thenReturn(config);
		when(context.getSechubJobUUID()).thenReturn(UUID.randomUUID());

		executorToTest = new TestNessusProductExecutor();
		executorToTest.installSetup=installSetup;

		executorToTest.nessusAdapter = nessusAdapter;

	}

	@Test
	public void when_three_root_urls_are_configured_and_apter_can_handle_target_the_adapter_is_called_1_times()
			throws Exception {
		/* prepare */
	    when(executorConfig.getProductIdentifier()).thenReturn(ProductIdentifier.NETSPARKER);
		when(installSetup.isAbleToScan(TargetType.INTERNET)).thenReturn(true);
		when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(false);

		prepareInfraScanWithThreeURIs();

		/* execute */
		executorToTest.execute(context,executorContext);

		/* test */
		verify(nessusAdapter, times(1)).start(any(),any());
	}

	@Test
	public void when_three_root_urls_are_configured_and_apter_can_handle_3_targets_with_two_different_types_the_adapter_is_called_1_time()
			throws Exception {
		/* prepare */
	    when(executorConfig.getProductIdentifier()).thenReturn(ProductIdentifier.NETSPARKER);
		Target target2ButIntranet = new Target(URI_2, TargetType.INTRANET);
		when(targetResolver.resolveTarget(URI_2)).thenReturn(target2ButIntranet);
		
		when(installSetup.isAbleToScan(TargetType.INTERNET)).thenReturn(true);
		when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(true);

		prepareInfraScanWithThreeURIs();

		/* execute */
		executorToTest.execute(context,executorContext);

		/* test */
		verify(nessusAdapter, times(2)).start(any(),any());
	}

	@Test
	public void when_three_root_urls_are_configured_and_apter_can_handle_2_targets_of_same_type_the_adapter_is_called_1_times()
			throws Exception {
		/* prepare */
	    when(executorConfig.getProductIdentifier()).thenReturn(ProductIdentifier.NETSPARKER);
		Target target2ButIntranet = new Target(URI_2, TargetType.INTRANET);
		when(targetResolver.resolveTarget(URI_2)).thenReturn(target2ButIntranet);
		
		when(installSetup.isAbleToScan(TargetType.INTERNET)).thenReturn(true);
		when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(false);
		
		prepareInfraScanWithThreeURIs();

		/* execute */
		executorToTest.execute(context,executorContext);

		/* test */
		verify(nessusAdapter, times(1)).start(any(),any());
	}
	@Test
	public void nessus_resolves_ip_from_infrascan() throws Exception{
		/* prepare */
	    when(executorConfig.getProductIdentifier()).thenReturn(ProductIdentifier.NESSUS);
		SecHubInfrastructureScanConfiguration infraScan = mock(SecHubInfrastructureScanConfiguration.class);
		List<InetAddress> expectedIPList = new ArrayList<>();
		when(infraScan.getIps()).thenReturn(expectedIPList);
		when(config.getInfraScan()).thenReturn(Optional.of(infraScan));
	
		/* execute */
		List<InetAddress> result = executorToTest.resolveInetAdressForTarget(config);
		
		/* test */
		assertEquals(expectedIPList,result);
	}

	
	
	@Test
	public void when_three_root_urls_are_configured_and_apter_cannot_handle_target_the_adapter_is_called_0_times()
			throws Exception {
		/* prepare */
		when(installSetup.isAbleToScan(TargetType.INTERNET)).thenReturn(false);
		when(installSetup.isAbleToScan(TargetType.INTRANET)).thenReturn(false);

		prepareInfraScanWithThreeURIs();

		/* execute */
		executorToTest.execute(context,executorContext);

		/* test */
		verify(nessusAdapter, never()).start(any(),any());
	}

	private void prepareInfraScanWithThreeURIs() throws URISyntaxException, SecHubExecutionException {
		SecHubInfrastructureScanConfiguration webscan = mock(SecHubInfrastructureScanConfiguration.class);
		when(config.getInfraScan()).thenReturn(Optional.of(webscan));
		List<URI> uris = new ArrayList<>();
		uris.add(URI_1);
		uris.add(URI_2);
		uris.add(URI_3);
		when(webscan.getUris()).thenReturn(uris);

	}

	/**
	 * Own class to testing purpose - we can mock here the target resolver to
	 * protected field
	 * 
	 * @author Albert Tregnaghi
	 *
	 */
	private class TestNessusProductExecutor extends NessusProductExecutor {
		private TestNessusProductExecutor() {
			super.targetResolver = NessusProductExecutorTest.this.targetResolver;
		}
	}

}
