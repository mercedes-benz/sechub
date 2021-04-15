// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.product.config.WithoutProductExecutorConfigInfo;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigInfo;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigRepository;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubInfrastructureScanConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

public class InfrastructureScanProductExecutionServiceImplTest {

	private static final String PROJECT_ID1 = "projectid1";
    private static final ProductIdentifier INFRASCANNER2_PRODUCT_IDENTIFIER = ProductIdentifier.NESSUS;
    private static final ProductIdentifier INFRASCANNER1_PRODUCT_IDENTIFIER = ProductIdentifier.PDS_INFRASCAN;
    private InfrastructureScanProductExecutionServiceImpl serviceToTest;
	private SecHubExecutionContext context;
	private SecHubConfiguration configuration;
	private SecHubInfrastructureScanConfiguration infraconfig;
	private URI uri;
	private InfrastructureScanProductExecutor infrascanner1;
	private InfrastructureScanProductExecutor infrascanner2;
    private ProductExecutorContext productExecutorContext;
    private ProductExecutorContextFactory productExecutorContextFactory;
    private ProductExecutorConfigRepository productExecutorConfigRepository;
    private ProductExecutorConfigInfo infraScanProductExecutorConfigInfo;

	@Before
	public void before() throws Exception {
	    infraScanProductExecutorConfigInfo= new WithoutProductExecutorConfigInfo(INFRASCANNER1_PRODUCT_IDENTIFIER);
	    
		uri = new URI("https://www.example.org");

		configuration = mock(SecHubConfiguration.class);
		when(configuration.getProjectId()).thenReturn(PROJECT_ID1);

		productExecutorContext=mock(ProductExecutorContext.class);
		
		infraconfig = mock(SecHubInfrastructureScanConfiguration.class);
		context = mock(SecHubExecutionContext.class);
		ProductResultRepository productResultRepository = mock(ProductResultRepository.class);

		infrascanner1 = mock(InfrastructureScanProductExecutor.class,"infrascanner1");
		when(infrascanner1.getIdentifier()).thenReturn(INFRASCANNER1_PRODUCT_IDENTIFIER);
		when(infrascanner1.getVersion()).thenReturn(1);
		
		infrascanner2 = mock(InfrastructureScanProductExecutor.class,"infrascanner2");
		when(infrascanner2.getIdentifier()).thenReturn(INFRASCANNER2_PRODUCT_IDENTIFIER);
        when(infrascanner2.getVersion()).thenReturn(2);

		when(infraconfig.getUris()).thenReturn(Collections.singletonList(uri));
		when(context.getConfiguration()).thenReturn(configuration);

		List<InfrastructureScanProductExecutor> executors = new ArrayList<>();
		executors.add(infrascanner1);
		executors.add(infrascanner2);
		
		productExecutorConfigRepository = mock(ProductExecutorConfigRepository.class);
		
		/* simulate default profile1*/
		when(productExecutorConfigRepository.findExecutableConfigurationsForProject(eq(PROJECT_ID1),eq(INFRASCANNER1_PRODUCT_IDENTIFIER),eq(1))).thenReturn(Arrays.asList(new ProductExecutorConfig(INFRASCANNER1_PRODUCT_IDENTIFIER,1, new ProductExecutorConfigSetup())));
        when(productExecutorConfigRepository.findExecutableConfigurationsForProject(eq(PROJECT_ID1),eq(INFRASCANNER2_PRODUCT_IDENTIFIER),eq(2))).thenReturn(Arrays.asList(new ProductExecutorConfig(INFRASCANNER2_PRODUCT_IDENTIFIER,2, new ProductExecutorConfigSetup())));

		serviceToTest = new InfrastructureScanProductExecutionServiceImpl(executors);
		serviceToTest.productResultRepository = productResultRepository;
		productExecutorContextFactory = mock(ProductExecutorContextFactory.class);
		serviceToTest.productExecutorContextFactory = productExecutorContextFactory;
		serviceToTest.productExecutorConfigRepository=productExecutorConfigRepository;

		when(productExecutorContextFactory.create(any(),any(), any(), any())).thenReturn(productExecutorContext);
		
	}

	@Test
	public void infrascanservice_executes_NO_registered_infrascan_product_executors_when_NO_infraconfig_available()
			throws Exception {

		/* prepare */
		when(configuration.getInfraScan()).thenReturn(Optional.empty());

		/* execute */
		serviceToTest.executeProductsAndStoreResults(context);

		/* test */
		verify(infrascanner1, never()).execute(context,productExecutorContext);
		verify(infrascanner2, never()).execute(context,productExecutorContext);

	}

	@Test
	public void infrascanservice_executes_registered_infrascan_product_executors_when_infraconfig_available()
			throws Exception {

		/* prepare */
		when(configuration.getInfraScan()).thenReturn(Optional.of(infraconfig));

		/* execute */
		serviceToTest.executeProductsAndStoreResults(context);

		/* test */
		verify(infrascanner1).execute(context,productExecutorContext);
		verify(infrascanner2).execute(context,productExecutorContext);

	}

	@Test
	public void infrascanservice_persists_2_results_of_2_registered_infrascan_product_executors() throws Exception {

		/* prepare */
		UUID secHubJobUUID = UUID.randomUUID();
		ProductResult result1 = new ProductResult(secHubJobUUID, "project1", infraScanProductExecutorConfigInfo, "result1");
		ProductResultTestAccess.setUUID(result1, UUID.randomUUID());

		ProductResult result2 = new ProductResult(secHubJobUUID, "project1", infraScanProductExecutorConfigInfo, "result2");
		ProductResultTestAccess.setUUID(result2, UUID.randomUUID());

		when(configuration.getInfraScan()).thenReturn(Optional.of(infraconfig));

		when(infrascanner1.execute(context,productExecutorContext)).thenReturn(Collections.singletonList(result1));
		when(infrascanner1.getIdentifier()).thenReturn(INFRASCANNER1_PRODUCT_IDENTIFIER);

		when(infrascanner2.execute(context,productExecutorContext)).thenReturn(Collections.singletonList(result2));
		when(infrascanner2.getIdentifier()).thenReturn(INFRASCANNER2_PRODUCT_IDENTIFIER);

		/* execute */
		serviceToTest.executeProductsAndStoreResults(context);

		/* test */
		verify(productExecutorContext).persist(result1);
		verify(productExecutorContext).persist(result2);

	}

	@Test
	public void infrascanservice_persists_3_results_of_2_registered_infrascan_product_executors() throws Exception {

		/* prepare */
		UUID secHubJobUUID = UUID.randomUUID();
		ProductResult result1 = new ProductResult(secHubJobUUID, "project1", infraScanProductExecutorConfigInfo, "result1");
		ProductResultTestAccess.setUUID(result1, UUID.randomUUID());

		ProductResult result2 = new ProductResult(secHubJobUUID, "project1", infraScanProductExecutorConfigInfo, "result2");
		ProductResultTestAccess.setUUID(result2, UUID.randomUUID());

		ProductResult result3 = new ProductResult(secHubJobUUID, "project1", infraScanProductExecutorConfigInfo, "result3");
		ProductResultTestAccess.setUUID(result3, UUID.randomUUID());

		when(configuration.getInfraScan()).thenReturn(Optional.of(infraconfig));

		when(infrascanner1.execute(context,productExecutorContext)).thenReturn(Collections.singletonList(result1));
		when(infrascanner1.getIdentifier()).thenReturn(INFRASCANNER1_PRODUCT_IDENTIFIER);

		List<ProductResult> list = new ArrayList<>();
		list.add(result2);
		list.add(result3);

		when(infrascanner2.execute(context,productExecutorContext)).thenReturn(list);
		when(infrascanner2.getIdentifier()).thenReturn(INFRASCANNER2_PRODUCT_IDENTIFIER);

		/* execute */
		serviceToTest.executeProductsAndStoreResults(context);

		/* test */
		verify(productExecutorContext).persist(result1);
		verify(productExecutorContext).persist(result2);
		verify(productExecutorContext).persist(result3);

	}

}
