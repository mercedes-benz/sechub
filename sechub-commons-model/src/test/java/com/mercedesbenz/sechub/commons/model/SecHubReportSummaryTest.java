package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SecHubReportSummaryTest {

    /**
     * The test is not needless: It is important inside
     * ScanReportToSecHubReportModelWithSummariesTransformer that the scan type
     * summaries are empty on creation time.
     *
     * As well as for JSON serialization.
     */
    @Test
    void intially_optionals_are_all_empty() {

        /* execute */
        SecHubReportSummary summary = new SecHubReportSummary();

        /* test */
        assertTrue(summary.getCodeScan().isEmpty());
        assertTrue(summary.getSecretScan().isEmpty());
        assertTrue(summary.getWebScan().isEmpty());
        assertTrue(summary.getLicenseScan().isEmpty());
        assertTrue(summary.getInfraScan().isEmpty());

    }

}
