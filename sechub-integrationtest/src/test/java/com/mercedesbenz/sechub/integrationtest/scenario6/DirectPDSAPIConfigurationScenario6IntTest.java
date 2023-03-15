// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.scenario6;

import static com.mercedesbenz.sechub.integrationtest.api.TestAPI.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.integrationtest.api.IntegrationTestSetup;

/**
 * Integration test directly using REST API of integration test PDS (means
 * without sechub server instance). When these tests fail, sechub tests will
 * also fail, because PDS API corrupt or PDS server not alive
 *
 * @author Albert Tregnaghi
 *
 */
public class DirectPDSAPIConfigurationScenario6IntTest {

    @Rule
    public IntegrationTestSetup setup = IntegrationTestSetup.forScenario(Scenario6.class);

    @Rule
    public Timeout timeOut = Timeout.seconds(600);

    @Test
    public void pds_admin_can_fetch_server_configuration() {
        /* @formatter:off */
        /* prepare */

        /* execute */
        String json = asPDSUser(PDS_ADMIN).getServerConfiguration();

        /* test */
        /* @formatter:off */
        assertJSON(json).
          fieldPathes().
            containsTextValue("PDS_INTTEST_PRODUCT_CODESCAN", "products","id").
            containsTextValue("./../sechub-integrationtest/pds/product-scripts/integrationtest-codescan.sh", "products","path").
            containsTextValue("product1.qualititycheck.enabled", "products","parameters","mandatory","key").
            containsTextValue("when 'true' quality scan results are added as well", "products","parameters","mandatory","description").
            containsTextValue("product1.add.tipoftheday", "products","parameters","optional","key").
            containsTextValue("add tip of the day as info", "products","parameters","optional","description");
        /* @formatter:on */

        /* @formatter:on */
    }

    @Test
    public void anonymous_cannot_fetch_server_configuration() {
        /* @formatter:off */
        /* execute + test */
        expectHttpFailure(()-> asPDSUser(ANONYMOUS).getServerConfiguration(), HttpStatus.UNAUTHORIZED);
        /* @formatter:on */
    }

    @Test
    public void pds_techuser_cannot_fetch_server_configuration() {
        /* @formatter:off */
        /* execute + test */
        expectHttpFailure(()-> asPDSUser(PDS_TECH_USER).getServerConfiguration(), HttpStatus.FORBIDDEN);
        /* @formatter:on */
    }

}
