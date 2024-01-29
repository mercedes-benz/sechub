// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario1;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.rules.Timeout;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData;
import com.mercedesbenz.sechub.integrationtest.internal.TestAutoCleanupData.TestCleanupTimeUnit;

/**
 * Inside these integration tests we only check that the inspector has been
 * called with 0 deletes (we have no old data - and we will not try to mock
 * this). The test does check that all expected domains are handled, which means
 * that the dedicated services are called automatically by a trigger service.
 * The logic itself about deletes, amount of deletes, calculation etc. is
 * already done inside DB tests and "normal" unit tests. So testing only for the
 * inspection calls is enough here.
 *
 */
public class AutoCleanupScenario1IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario1.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void auto_cleanup_executed_in_every_domain_when_admin_configures_cleanupdays_1() {

        /* prepare */
        resetAutoCleanupDays(0);

        /* execute */
        as(SUPER_ADMIN).updateAutoCleanupConfiguration(new TestAutoCleanupData(1, TestCleanupTimeUnit.DAY));

        /* test */
        /* @formatter:off */
        waitUntilEveryDomainHasAutoCleanupSynchedToDays(1);

        assertAutoCleanupInspections().

            /* administration */
            addExpectedDeleteInspection("job-information","com.mercedesbenz.sechub.domain.administration.autocleanup.AdministrationAutoCleanupService",0).

            /* scan domain */
            addExpectedDeleteInspection("scan-logs","com.mercedesbenz.sechub.domain.scan.autocleanup.ScanAutoCleanupService",0).

            /* schedule domain */
            addExpectedDeleteInspection("sechub-jobs","com.mercedesbenz.sechub.domain.schedule.autocleanup.ScheduleAutoCleanupService",0).
            addExpectedDeleteInspection("product-results","com.mercedesbenz.sechub.domain.scan.autocleanup.ScanAutoCleanupService",0).
            addExpectedDeleteInspection("scan-reports","com.mercedesbenz.sechub.domain.scan.autocleanup.ScanAutoCleanupService",0).

            addExpectedDifferentKindOfDeleteInspections(5).

            assertAsExpectedWithTimeOut(15);

        /* @formatter:on */

    }

    @Test
    public void auto_cleanup_NEVER_executed_in_any_domain_when_admin_configures_cleanupdays_0() {

        /* prepare */
        resetAutoCleanupDays(4711);// so different to 0 which we will set next

        /* execute */
        as(SUPER_ADMIN).updateAutoCleanupConfiguration(new TestAutoCleanupData(0, TestCleanupTimeUnit.DAY));

        /* test */
        /* @formatter:off */
        waitUntilEveryDomainHasAutoCleanupSynchedToDays(0);

        assertAutoCleanupInspections().
            addExpectedNeverAnyDeleteInspection(). // this will take 5 seconds

            assertAsExpectedWithTimeOut(3);

        /* @formatter:on */

    }

    @AfterAll
    static void afterAll() {
        /* disable again */
        resetAutoCleanupDays(0);
    }

}
