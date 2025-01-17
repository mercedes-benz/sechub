// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.integrationtest.api.TestOnlyForRegularExecution;

@TestOnlyForRegularExecution
public class SecHubReportFileNameFilterTest {

    @Test
    public void accepted_when_same_uuid_inside() {
        /* prepare */
        SecHubReportFileNameFilter filter = new SecHubReportFileNameFilter(UUID.fromString("2a75aa71-79b1-4e27-bcfa-20705fee84dd"));
        File fileToCheck = IntegrationTestFileSupport.getTestfileSupport()
                .createFileFromResourcePath("report/sechub_report_test_2a75aa71-79b1-4e27-bcfa-20705fee84dd.json");

        /* execute */
        boolean accepted = filter.accept(fileToCheck.getParentFile(), fileToCheck.getName());

        /* test */
        assertTrue(accepted, "file should be accepted:" + fileToCheck);
    }

    @Test
    public void not_accepted_when_other_uuid() {
        /* prepare */
        SecHubReportFileNameFilter filter = new SecHubReportFileNameFilter(UUID.fromString("2a75aa71-79b1-4e27-bcfa-20705fee84dd"));
        File fileToCheck = IntegrationTestFileSupport.getTestfileSupport()
                .createFileFromResourcePath("report/sechub_report_test_cb2e41ca-1363-47a4-8308-8c91cc022620.json");

        /* execute */
        boolean accepted = filter.accept(fileToCheck.getParentFile(), fileToCheck.getName());

        /* test */
        assertFalse(accepted, "File should NOT be accepted" + fileToCheck);
    }

}
