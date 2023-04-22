// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubSecretScanConfiguration;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class SecretScanProductExecutionServiceImplTest {
    private SecretScanProductExecutionServiceImpl secretScanServiceToTest;

    @BeforeEach
    void beforeEach() {
        secretScanServiceToTest = new SecretScanProductExecutionServiceImpl();
    }

    @Test
    void getScanType() {
        /* execute + test */
        assertEquals(ScanType.SECRET_SCAN, secretScanServiceToTest.getScanType());
    }

    @Test
    void isExecutionNecessary_unnecessary() {
        /* prepare */
        UUIDTraceLogID traceId = UUIDTraceLogID.traceLogID(UUID.randomUUID());
        SecHubExecutionContext context = mock(SecHubExecutionContext.class);
        SecHubConfiguration configuration = new SecHubConfiguration();

        /* execute + test */
        assertFalse(secretScanServiceToTest.isExecutionNecessary(context, traceId, configuration));
    }

    @Test
    void isExecutionNecessary_necessary() {
        /* prepare */
        UUIDTraceLogID traceId = UUIDTraceLogID.traceLogID(UUID.randomUUID());
        SecHubExecutionContext context = mock(SecHubExecutionContext.class);
        SecHubConfiguration configuration = new SecHubConfiguration();
        SecHubSecretScanConfiguration secretScanConfig = new SecHubSecretScanConfiguration();
        configuration.setSecretScan(secretScanConfig);

        /* execute + test */
        assertTrue(secretScanServiceToTest.isExecutionNecessary(context, traceId, configuration));
    }
}
