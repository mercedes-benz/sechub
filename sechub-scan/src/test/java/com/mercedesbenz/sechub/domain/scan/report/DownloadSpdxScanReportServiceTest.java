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
import com.mercedesbenz.sechub.domain.scan.resolve.SpdxJsonResolver;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

class DownloadSpdxScanReportServiceTest {

    private DownloadSpdxScanReportService serviceToTest;
    private UserInputAssertion assertion;
    private AuditLogService auditLogService;
    private ProductResultRepository productResultRepository;
    private ScanAssertService scanAssertService;
    private SpdxJsonResolver spdxJsonResolver;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new DownloadSpdxScanReportService();

        assertion = mock(UserInputAssertion.class);
        auditLogService = mock(AuditLogService.class);
        productResultRepository = mock(ProductResultRepository.class);
        scanAssertService = mock(ScanAssertService.class);
        spdxJsonResolver = mock(SpdxJsonResolver.class);

        serviceToTest.assertion = assertion;
        serviceToTest.auditLogService = auditLogService;
        serviceToTest.productResultRepository = productResultRepository;
        serviceToTest.scanAssertService = scanAssertService;
        serviceToTest.spdxJsonResolver = spdxJsonResolver;
    }

    @Test
    void auditLogging_jobUuid_test() {
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
    void return_result_if_not_null() {
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
    void return_result_null_throws_exception() {
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
