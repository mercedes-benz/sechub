// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.scan.ScanAssertService;
import com.mercedesbenz.sechub.domain.scan.product.ProductIdentifier;
import com.mercedesbenz.sechub.domain.scan.product.ProductResult;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.resolve.ProductResultSpdxJsonResolver;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class DownloadSpdxScanReportServiceTest {

    private DownloadSpdxScanReportService serviceToTest;
    private UserInputAssertion assertion;
    private AuditLogService auditLogService;
    private ProductResultRepository productResultRepository;
    private ScanAssertService scanAssertService;
    private ProductResultSpdxJsonResolver spdxJsonResolver;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new DownloadSpdxScanReportService();

        assertion = mock(UserInputAssertion.class);
        auditLogService = mock(AuditLogService.class);
        productResultRepository = mock(ProductResultRepository.class);
        scanAssertService = mock(ScanAssertService.class);
        spdxJsonResolver = mock(ProductResultSpdxJsonResolver.class);

        serviceToTest.assertion = assertion;
        serviceToTest.auditLogService = auditLogService;
        serviceToTest.productResultRepository = productResultRepository;
        serviceToTest.scanAssertService = scanAssertService;
        serviceToTest.spdxJsonResolver = spdxJsonResolver;
    }

    @Test
    void audit_log_contains_spdx_json_report_with_job_uuid() {
        /* prepare */
        UUID sechubJobUUID = UUID.randomUUID();

        ProductResult result = mock(ProductResult.class);
        List<ProductResult> results = new ArrayList<>();
        results.add(result);

        when(productResultRepository.findAllProductResults(eq(sechubJobUUID), eq(ProductIdentifier.SERECO))).thenReturn(results);
        when(spdxJsonResolver.resolveSpdxJson(eq(result))).thenReturn("{}");

        /* execute */
        serviceToTest.getScanSpdxJsonReport("project_1", sechubJobUUID);

        /* test */
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> param1 = ArgumentCaptor.forClass(Object.class);

        verify(auditLogService).log(message.capture(), param1.capture());
        String logMessage = message.getValue();
        Object logParam1 = param1.getValue();

        assertTrue(logMessage.contains("SPDX Json report"));
        assertEquals(sechubJobUUID, logParam1);
    }

    @Test
    void service_returns_spdx_json_resolver_result_for_sereco_productresult() {
        /* prepare */
        UUID sechubJobUUID = UUID.randomUUID();

        ProductResult result = mock(ProductResult.class);
        List<ProductResult> results = new ArrayList<>();
        results.add(result);

        String expectedJson = "my result";

        when(productResultRepository.findAllProductResults(eq(sechubJobUUID), eq(ProductIdentifier.SERECO))).thenReturn(results);
        when(spdxJsonResolver.resolveSpdxJson(eq(result))).thenReturn(expectedJson);

        /* execute */
        String spdxJson = serviceToTest.getScanSpdxJsonReport("project_1", sechubJobUUID);

        /* test */
        assertEquals(expectedJson, spdxJson);
    }

    @Test
    void service_throws_not_found_exception_when_spdx_json_resolver_result_is_null_for_sereco_productresult() {
        /* prepare */
        UUID sechubJobUUID = UUID.randomUUID();

        ProductResult result = mock(ProductResult.class);
        List<ProductResult> results = new ArrayList<>();
        results.add(result);

        when(productResultRepository.findAllProductResults(eq(sechubJobUUID), eq(ProductIdentifier.SERECO))).thenReturn(results);
        when(spdxJsonResolver.resolveSpdxJson(eq(result))).thenReturn(null);

        /* execute + test */
        assertThrows(NotFoundException.class, () -> serviceToTest.getScanSpdxJsonReport("project_1", sechubJobUUID));
    }
}
