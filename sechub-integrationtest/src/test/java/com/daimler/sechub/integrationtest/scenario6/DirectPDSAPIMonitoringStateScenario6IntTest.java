// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario6;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.springframework.http.HttpStatus;

import com.daimler.sechub.integrationtest.api.IntegrationTestSetup;

/**
 * Integration test directly using REST API of integration test PDS (means
 * without sechub). When these tests fail, those sechub tests which are relying
 * on a running PDS will also fail, because PDS API corrupt or PDS server not
 * alive
 * 
 * @author Albert Tregnaghi
 *
 */
public class DirectPDSAPIMonitoringStateScenario6IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario6.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void pds_admin_can_fetch_execution_state() {
        /* execute */
        String json = asPDSUser(PDS_ADMIN).getMonitoringStatus();
        System.out.println(json);
        /* test */
        /* @formatter:off */
        assertJSON(json).
            containsText("CREATED").
            containsText("QUEUED").
            containsText("READY_TO_START").
            containsText("RUNNING").
            containsText("DONE").
            fieldPathes().
                containsTextValue("50", "queueMax"); // all other values could differ on runtime , so this check is enough. Here we only check that config can be fetched
        /* @formatter:on */
    }

    @Test
    public void anonymous_cannot_fetch_execution_state() {
        /* execute +test */
        expectHttpFailure(() -> asPDSUser(ANONYMOUS).getMonitoringStatus(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void pds_techuser_cannot_fetch_execution_state() {
        /* execute +test */
        expectHttpFailure(() -> asPDSUser(PDS_TECH_USER).getMonitoringStatus(), HttpStatus.FORBIDDEN);
    }

}
