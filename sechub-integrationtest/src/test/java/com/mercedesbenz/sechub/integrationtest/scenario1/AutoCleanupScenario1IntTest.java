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
    public void auto_cleanup_multi_test() {
        /*
         * why is this a multi test? Because we cannot predict the ordering in different
         * junit tests and there were flaky test situtions, the both tests are combined
         * here and run different.
         *
         * Inside the stacktrace we can still see at which "stage" the test fail
         */

        /* first we configure and expect events */
        auto_cleanup_executed_in_every_domain_when_admin_configures_cleanupdays_1();

        /* next reset t0 0 and expect no longer events */
        auto_cleanup_NEVER_executed_in_any_domain_when_admin_configures_cleanupdays_0();
    }

    private void auto_cleanup_executed_in_every_domain_when_admin_configures_cleanupdays_1() {

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

    private void auto_cleanup_NEVER_executed_in_any_domain_when_admin_configures_cleanupdays_0() {

        /* execute */
        as(SUPER_ADMIN).updateAutoCleanupConfiguration(new TestAutoCleanupData(0, TestCleanupTimeUnit.DAY));

        /* test */
        /* @formatter:off */
        waitUntilEveryDomainHasAutoCleanupSynchedToDays(0);
        waitMilliSeconds(500); // give server chance to trigger/handle last remaining events
        resetIntegrationTestAutoCleanupInspectorEvents(); // clear existing events

        // now we check that even after 3 seconds, there is no interaction/no new events
        // why 3 seconds? Please look at application-integrationtest.yml: auto clean trigger is here set to
        // delay of 2 seconds, initial delay 100 milliseconds.
        // when we wait 3 seconds the next trigger would be done, if it would not be turned off!
        // (we test cleanup is no longer triggered when set to 0)
        assertAutoCleanupInspections().
            addExpectedNeverAnyDeleteInspection().

            assertAsExpectedWithTimeOut(3,1000); // we have here always a retry, means check is done here for a given time of 3 seconds

        /* @formatter:on */

    }

    @AfterAll
    static void afterAll() {
        /* disable again */
        resetAutoCleanupDays(0);
    }

}
