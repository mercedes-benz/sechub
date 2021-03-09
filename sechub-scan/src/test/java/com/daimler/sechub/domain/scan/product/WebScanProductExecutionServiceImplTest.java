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
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigRepository;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.configuration.SecHubWebScanConfiguration;
import com.daimler.sechub.sharedkernel.execution.SecHubExecutionContext;

public class WebScanProductExecutionServiceImplTest {

    private static final String PROJECT_ID1 = "projectid1";
    private static final ProductIdentifier PRODUCT_IDENTIFIER_2 = ProductIdentifier.NETSPARKER;
    private static final ProductIdentifier PRODUCT_IDENTIFIER_1 = ProductIdentifier.PDS_CODESCAN;
    private WebScanProductExecutionServiceImpl serviceToTest;
    private SecHubExecutionContext context;
    private SecHubConfiguration configuration;
    private SecHubWebScanConfiguration webconfiguration;
    private URI uri;
    private WebScanProductExecutor webscanner1;
    private WebScanProductExecutor webscanner2;
    private ProductExecutorContext productExecutorContext;
    private ProductExecutorContextFactory productExecutorContextFactory;
    private ProductExecutorConfigRepository productExecutorConfigRepository;

    @Before
    public void before() throws Exception {
        uri = new URI("https://www.example.org");

        configuration = mock(SecHubConfiguration.class);
        when(configuration.getProjectId()).thenReturn(PROJECT_ID1);

        productExecutorContext = mock(ProductExecutorContext.class);
        webconfiguration = mock(SecHubWebScanConfiguration.class);
        context = mock(SecHubExecutionContext.class);
        ProductResultRepository productResultRepository = mock(ProductResultRepository.class);

        webscanner1 = mock(WebScanProductExecutor.class);
        when(webscanner1.getIdentifier()).thenReturn(PRODUCT_IDENTIFIER_1);
        when(webscanner1.getVersion()).thenReturn(1);
        webscanner2 = mock(WebScanProductExecutor.class);
        when(webscanner2.getIdentifier()).thenReturn(PRODUCT_IDENTIFIER_2);
        when(webscanner2.getVersion()).thenReturn(2);

        when(webconfiguration.getUris()).thenReturn(Collections.singletonList(uri));
        when(context.getConfiguration()).thenReturn(configuration);

        List<WebScanProductExecutor> executors = new ArrayList<>();
        executors.add(webscanner1);
        executors.add(webscanner2);

        productExecutorConfigRepository = mock(ProductExecutorConfigRepository.class);

        /* simulate default profile1 */
        when(productExecutorConfigRepository.findExecutableConfigurationsForProject(eq(PROJECT_ID1), eq(PRODUCT_IDENTIFIER_1), eq(1)))
                .thenReturn(Arrays.asList(new ProductExecutorConfig(PRODUCT_IDENTIFIER_1, 1, new ProductExecutorConfigSetup())));
        when(productExecutorConfigRepository.findExecutableConfigurationsForProject(eq(PROJECT_ID1), eq(PRODUCT_IDENTIFIER_2), eq(2)))
                .thenReturn(Arrays.asList(new ProductExecutorConfig(PRODUCT_IDENTIFIER_2, 2, new ProductExecutorConfigSetup())));

        serviceToTest = new WebScanProductExecutionServiceImpl(executors);
        serviceToTest.productResultRepository = productResultRepository;

        productExecutorContextFactory = mock(ProductExecutorContextFactory.class);
        serviceToTest.productExecutorContextFactory = productExecutorContextFactory;
        serviceToTest.productExecutorConfigRepository = productExecutorConfigRepository;

        when(productExecutorContextFactory.create(any(), any(), any(), any())).thenReturn(productExecutorContext);

    }

    @Test
    public void webscanservice_executes_NO_registered_webscan_product_executors_when_NO_webconfiguration_available() throws Exception {

        /* prepare */
        when(configuration.getWebScan()).thenReturn(Optional.empty());

        /* execute */
        serviceToTest.executeProductsAndStoreResults(context);

        /* test */
        verify(webscanner1, never()).execute(context, productExecutorContext);
        verify(webscanner2, never()).execute(context, productExecutorContext);

    }

    @Test
    public void webscanservice_executes_registered_webscan_product_executors_when_webconfiguration_available() throws Exception {

        /* prepare */
        when(configuration.getWebScan()).thenReturn(Optional.of(webconfiguration));

        /* execute */
        serviceToTest.executeProductsAndStoreResults(context);

        /* test */
        verify(webscanner1).execute(context, productExecutorContext);
        verify(webscanner2).execute(context, productExecutorContext);

    }

    @Test
    public void webscanservice_persists_2_results_of_2_registered_webscan_product_executors() throws Exception {

        /* prepare */
        UUID secHubJobUUID = UUID.randomUUID();
        ProductResult result1 = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(PRODUCT_IDENTIFIER_1), "result1");
        ProductResultTestAccess.setUUID(result1, UUID.randomUUID());

        ProductResult result2 = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(PRODUCT_IDENTIFIER_2), "result2");
        ProductResultTestAccess.setUUID(result2, UUID.randomUUID());

        when(configuration.getWebScan()).thenReturn(Optional.of(webconfiguration));

        when(webscanner1.execute(context, productExecutorContext)).thenReturn(Collections.singletonList(result1));
        when(webscanner1.getIdentifier()).thenReturn(PRODUCT_IDENTIFIER_1);

        when(webscanner2.execute(context, productExecutorContext)).thenReturn(Collections.singletonList(result2));
        when(webscanner2.getIdentifier()).thenReturn(PRODUCT_IDENTIFIER_2);

        /* execute */
        serviceToTest.executeProductsAndStoreResults(context);

        /* test */
        verify(productExecutorContext).persist(result1);
        verify(productExecutorContext).persist(result2);

    }

    @Test
    public void webscanservice_persists_3_results_of_2_registered_webscan_product_executors() throws Exception {

        /* prepare */
        UUID secHubJobUUID = UUID.randomUUID();
        ProductResult result1 = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(PRODUCT_IDENTIFIER_1), "result1");
        ProductResultTestAccess.setUUID(result1, UUID.randomUUID());

        ProductResult result2 = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(PRODUCT_IDENTIFIER_2), "result2");
        ProductResultTestAccess.setUUID(result2, UUID.randomUUID());

        ProductResult result3 = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(PRODUCT_IDENTIFIER_2), "result3");
        ProductResultTestAccess.setUUID(result3, UUID.randomUUID());

        when(configuration.getWebScan()).thenReturn(Optional.of(webconfiguration));

        when(webscanner1.execute(context, productExecutorContext)).thenReturn(Collections.singletonList(result1));
        when(webscanner1.getIdentifier()).thenReturn(PRODUCT_IDENTIFIER_1);

        List<ProductResult> list = new ArrayList<>();
        list.add(result2);
        list.add(result3);

        when(webscanner2.execute(context, productExecutorContext)).thenReturn(list);
        when(webscanner2.getIdentifier()).thenReturn(PRODUCT_IDENTIFIER_2);

        /* execute */
        serviceToTest.executeProductsAndStoreResults(context);

        /* test */
        verify(productExecutorContext).persist(result1);
        verify(productExecutorContext).persist(result2);
        verify(productExecutorContext).persist(result3);

    }

}
