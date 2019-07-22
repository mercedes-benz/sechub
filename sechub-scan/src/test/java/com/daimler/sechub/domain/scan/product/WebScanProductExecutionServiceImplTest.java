// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product;

import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubWebScanConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

public class WebScanProductExecutionServiceImplTest {

	private WebScanProductExecutionServiceImpl serviceToTest;
	private SecHubExecutionContext context;
	private SecHubConfiguration configuration;
	private SecHubWebScanConfiguration webconfiguration;
	private URI uri;
	private ProductResultRepository productResultRepository;
	private WebScanProductExecutor webscanner1;
	private WebScanProductExecutor webscanner2;

	@Before
	public void before() throws Exception {
		uri = new URI("https://www.example.org");

		configuration = mock(SecHubConfiguration.class);
		webconfiguration = mock(SecHubWebScanConfiguration.class);
		context = mock(SecHubExecutionContext.class);
		productResultRepository = mock(ProductResultRepository.class);

		webscanner1 = mock(WebScanProductExecutor.class);
		webscanner2 = mock(WebScanProductExecutor.class);

		when(webconfiguration.getUris()).thenReturn(Collections.singletonList(uri));
		when(context.getConfiguration()).thenReturn(configuration);

		List<WebScanProductExecutor> executors = new ArrayList<>();
		executors.add(webscanner1);
		executors.add(webscanner2);

		serviceToTest = new WebScanProductExecutionServiceImpl(executors);
		serviceToTest.productResultRepository = productResultRepository;
	}

	@Test
	public void webscanservice_executes_NO_registered_webscan_product_executors_when_NO_webconfiguration_available()
			throws Exception {

		/* prepare */
		when(configuration.getWebScan()).thenReturn(Optional.empty());

		/* execute */
		serviceToTest.executeProductsAndStoreResults(context);

		/* test */
		verify(webscanner1, never()).execute(context);
		verify(webscanner2, never()).execute(context);

	}

	@Test
	public void webscanservice_executes_registered_webscan_product_executors_when_webconfiguration_available()
			throws Exception {

		/* prepare */
		when(configuration.getWebScan()).thenReturn(Optional.of(webconfiguration));

		/* execute */
		serviceToTest.executeProductsAndStoreResults(context);

		/* test */
		verify(webscanner1).execute(context);
		verify(webscanner2).execute(context);

	}

	@Test
	public void webscanservice_persists_2_results_of_2_registered_webscan_product_executors() throws Exception {

		/* prepare */
		UUID secHubJobUUID = UUID.randomUUID();
		ProductResult result1 = new ProductResult(secHubJobUUID, ProductIdentifier.FARRADAY, "result1");
		ProductResultTestAccess.setUUID(result1, UUID.randomUUID());

		ProductResult result2 = new ProductResult(secHubJobUUID, ProductIdentifier.NETSPARKER, "result2");
		ProductResultTestAccess.setUUID(result2, UUID.randomUUID());

		when(configuration.getWebScan()).thenReturn(Optional.of(webconfiguration));

		when(webscanner1.execute(context)).thenReturn(Collections.singletonList(result1));
		when(webscanner1.getIdentifier()).thenReturn(ProductIdentifier.FARRADAY);

		when(webscanner2.execute(context)).thenReturn(Collections.singletonList(result2));
		when(webscanner2.getIdentifier()).thenReturn(ProductIdentifier.NETSPARKER);

		/* execute */
		serviceToTest.executeProductsAndStoreResults(context);

		/* test */
		verify(productResultRepository).save(result1);
		verify(productResultRepository).save(result2);

	}
	
	@Test
	public void webscanservice_persists_3_results_of_2_registered_webscan_product_executors() throws Exception {

		/* prepare */
		UUID secHubJobUUID = UUID.randomUUID();
		ProductResult result1 = new ProductResult(secHubJobUUID, ProductIdentifier.FARRADAY, "result1");
		ProductResultTestAccess.setUUID(result1, UUID.randomUUID());

		ProductResult result2 = new ProductResult(secHubJobUUID, ProductIdentifier.NETSPARKER, "result2");
		ProductResultTestAccess.setUUID(result2, UUID.randomUUID());
		
		ProductResult result3 = new ProductResult(secHubJobUUID, ProductIdentifier.NETSPARKER, "result3");
		ProductResultTestAccess.setUUID(result3, UUID.randomUUID());

		when(configuration.getWebScan()).thenReturn(Optional.of(webconfiguration));

		when(webscanner1.execute(context)).thenReturn(Collections.singletonList(result1));
		when(webscanner1.getIdentifier()).thenReturn(ProductIdentifier.FARRADAY);

		List<ProductResult> list = new ArrayList<>();
		list.add(result2);
		list.add(result3);
		
		when(webscanner2.execute(context)).thenReturn(list);
		when(webscanner2.getIdentifier()).thenReturn(ProductIdentifier.NETSPARKER);

		/* execute */
		serviceToTest.executeProductsAndStoreResults(context);

		/* test */
		verify(productResultRepository).save(result1);
		verify(productResultRepository).save(result2);
		verify(productResultRepository).save(result3);

	}

}
