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
import com.mercedesbenz.sechub.commons.model.SecHubLicenseScanConfiguration;
import com.mercedesbenz.sechub.domain.scan.SecHubExecutionContext;
import com.mercedesbenz.sechub.sharedkernel.UUIDTraceLogID;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class LicenseScanProductExecutionServiceImplTest {
    private LicenseScanProductExecutionServiceImpl licenseScanServiceToTest;

    @BeforeEach
    void beforeEach() {
        licenseScanServiceToTest = new LicenseScanProductExecutionServiceImpl();
    }

    @Test
    void getScanType() {
        /* execute + test */
        assertEquals(ScanType.LICENSE_SCAN, licenseScanServiceToTest.getScanType());
    }

    @Test
    void isExecutionNecessary_unnecessary() {
        /* prepare */
        UUIDTraceLogID traceId = UUIDTraceLogID.traceLogID(UUID.randomUUID());
        SecHubExecutionContext context = mock(SecHubExecutionContext.class);
        SecHubConfiguration configuration = new SecHubConfiguration();

        /* execute + test */
        assertFalse(licenseScanServiceToTest.isExecutionNecessary(context, traceId, configuration));
    }

    @Test
    void isExecutionNecessary_necessary() {
        /* prepare */
        UUIDTraceLogID traceId = UUIDTraceLogID.traceLogID(UUID.randomUUID());
        SecHubExecutionContext context = mock(SecHubExecutionContext.class);
        SecHubConfiguration configuration = new SecHubConfiguration();
        SecHubLicenseScanConfiguration licenseScanConfig = new SecHubLicenseScanConfiguration();
        configuration.setLicenseScan(licenseScanConfig);

        /* execute + test */
        assertTrue(licenseScanServiceToTest.isExecutionNecessary(context, traceId, configuration));
    }
}
