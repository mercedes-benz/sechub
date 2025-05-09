// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.product.config.WithoutProductExecutorConfigInfo;
import com.mercedesbenz.sechub.domain.scan.report.ReportProductResultTransformer;
import com.mercedesbenz.sechub.sharedkernel.ProductIdentifier;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class SecHubReportProductTransformerServiceTest {

    private SecHubReportProductTransformerService serviceToTest;
    private ReportProductResultTransformer reportTransformer;
    private ProductResultRepository productResultRepository;
    private SecHubExecutionContext context;
    private UUID secHubJobUUID;
    private ReportTransformationResultMerger resultMerger;

    @BeforeEach
    void beforeEach() throws Exception {
        secHubJobUUID = UUID.randomUUID();
        context = mock(SecHubExecutionContext.class);
        when(context.getSechubJobUUID()).thenReturn(secHubJobUUID);
        when(context.getConfiguration()).thenReturn(new SecHubConfiguration());

        reportTransformer = mock(ReportProductResultTransformer.class);
        productResultRepository = mock(ProductResultRepository.class);
        resultMerger = mock(ReportTransformationResultMerger.class);

        serviceToTest = new SecHubReportProductTransformerService();
        serviceToTest.transformers = Collections.singletonList(reportTransformer);
        serviceToTest.productResultRepository = productResultRepository;
        serviceToTest.resultMerger = resultMerger;
    }

    @Test
    void when_product_result_repository_returns_empty_list__sechub_execution_is_thrown_with_message() throws Exception {
        /* prepare */
        when(productResultRepository.findAllProductResults(eq(secHubJobUUID), any())).thenReturn(new ArrayList<>());

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.createResult(context)).isInstanceOf(SecHubExecutionException.class)
                .hasMessage("No report result found for:" + secHubJobUUID);
    }

    @SuppressWarnings("deprecation")
    @Test
    void when_product_result_repository_returns_only_netsparker_result__sechub_execution_is_thrown_with_message() throws Exception {
        /* prepare */
        ProductResult scanResult = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.NETSPARKER),
                "scan-result");

        when(productResultRepository.findAllProductResults(eq(secHubJobUUID), any())).thenReturn(Arrays.asList(scanResult));

        /* execute + test */
        assertThatThrownBy(() -> serviceToTest.createResult(context)).isInstanceOf(SecHubExecutionException.class)
                .hasMessageContaining("No transformable report result format found").hasMessageContaining(secHubJobUUID.toString());
    }

    @Test
    void when_product_result_repository_returns_only_sereco_report_result__sechubResultFromTransformer_is_returned() throws Exception {
        /* prepare */
        ReportTransformationResult transformationResult = new ReportTransformationResult();
        ProductResult scanResult = new ProductResult(secHubJobUUID, "project1", new WithoutProductExecutorConfigInfo(ProductIdentifier.SERECO), "scan-result");
        when(reportTransformer.canTransform(ProductIdentifier.SERECO)).thenReturn(true);
        when(reportTransformer.transform(scanResult)).thenReturn(transformationResult);

        when(productResultRepository.findAllProductResults(eq(secHubJobUUID), any())).thenReturn(Arrays.asList(scanResult));
        when(resultMerger.merge(null, transformationResult)).thenReturn(transformationResult);

        /* execute */
        ReportTransformationResult result = serviceToTest.createResult(context);

        /* test */
        assertThat(transformationResult).isEqualTo(result);

    }

    @ParameterizedTest
    @ArgumentsSource(ScanTypeSetArgumentsProvider.class)
    void create_result_sets_public_scan_types_from_execution_context_to_result(String variant, Set<ScanType> scanTypes) throws Exception {
        /* prepare */
        ReportTransformationResult mergerResult = new ReportTransformationResult();

        ProductResult scanResult = mock(ProductResult.class);
        when(scanResult.getProductIdentifier()).thenReturn(ProductIdentifier.PDS_CODESCAN);
        when(reportTransformer.canTransform(ProductIdentifier.PDS_CODESCAN)).thenReturn(true);

        when(reportTransformer.transform(scanResult)).thenReturn(mergerResult);

        when(productResultRepository.findAllProductResults(eq(secHubJobUUID), any())).thenReturn(Arrays.asList(scanResult));
        when(resultMerger.merge(null, mergerResult)).thenReturn(mergerResult);

        // here is the important part: context has public scan types:
        when(context.getUsedPublicScanTypes()).thenReturn(scanTypes);

        /* execute */
        ReportTransformationResult result = serviceToTest.createResult(context);

        /* test */
        assertThat(result.getMetaData().getExecuted()).containsAll(scanTypes);
    }

    private static class ScanTypeSetArgumentsProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
              Arguments.of("a1", Set.of()),
              Arguments.of("a2", Set.of(ScanType.CODE_SCAN)),
              Arguments.of("a3", Set.of(ScanType.WEB_SCAN)),
              Arguments.of("a4", Set.of(ScanType.IAC_SCAN, ScanType.CODE_SCAN)),
              Arguments.of("a5", Set.of(ScanType.INFRA_SCAN)),
		      Arguments.of("a6", Set.of(ScanType.LICENSE_SCAN, ScanType.CODE_SCAN)));
        }
        /* @formatter:on*/
    }

}
