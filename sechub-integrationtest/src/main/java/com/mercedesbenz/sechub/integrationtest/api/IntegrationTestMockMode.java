// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.net.URI;
import java.net.URISyntaxException;

import com.mercedesbenz.sechub.integrationtest.internal.MockData;

/**
 * See <code>mockdata_setup.json</code> for configuration parts
 *
 * @author Albert Tregnaghi
 *
 */
public enum IntegrationTestMockMode {

    /**
     * No waits for executed mocks.
     */
    WEBSCAN__NETSPARKER_GREEN__ZERO_WAIT(MockData.NETSPARKER_GREEN_ZERO_WAIT),

    /**
     * Web and infra scans will have 10 seconds elapse time on mock execution.
     */
    WEBSCAN__NETSPARKER_GREEN__10_SECONDS_WAITING(MockData.NETSPARKER_GREEN_10_SECONDS_WAITING),

    /**
     * Has one RED finding
     */
    WEBSCAN__NETSPARKER_RED__ZERO_WAIT(MockData.NETSPARKER_RED_ZERO_WAIT),

    WEBSCAN__NETSPARKER_MULTI__ZERO_WAIT(MockData.NETSPARKER_MULTI_ZERO_WAIT),

    /**
     * Results in traffic light yellow
     */
    CODE_SCAN__CHECKMARX__MULTI__ZERO_WAIT(MockData.CHECKMARX_MULTI_ZERO_WAIT),

    /**
     * runs 1 second - results in green
     */
    CODE_SCAN__CHECKMARX__GREEN__1_SECOND_WAITING(MockData.CHECKMARX_GREEN_1_SECOND_WAITING),

    /**
     * runs 1 second - results in green
     */
    CODE_SCAN__CHECKMARX__GREEN__ZERO_WAIT(MockData.CHECKMARX_GREEN_ZERO_WAIT),

    /**
     * runs 10 milliseconds - results in green
     */
    CODE_SCAN__CHECKMARX__GREEN__10_MS_WATING(MockData.CHECKMARX_GREEN_10_MILLIS_WAITING),

    /**
     * runs 4 seconds - results in green
     */
    CODE_SCAN__CHECKMARX__GREEN__4_SECONDS_WAITING(MockData.CHECKMARX_GREEN_4_SECONDS_WAITING),

    /**
     * Not predefined - means there is no predefined mock data available
     */
    NOT_PREDEFINED(null),

    /**
     * Just do no mocking - we use this for example at PDS communications where we
     * have a real (integration test) PDS instance running.
     */
    NOT_MOCKED(null),

    ;

    private String target;
    private boolean isTargetUsableAsWhiteListEntry;

    private IntegrationTestMockMode(MockData mockData) {

        this.target = mockData != null ? mockData.getTarget() : null;
        isTargetUsableAsWhiteListEntry = false;
        if (target != null) {
            try {
                new URI(target);
                isTargetUsableAsWhiteListEntry = true;
            } catch (URISyntaxException e) {
                /* means is no URI */
            }
        }
    }

    public String getTarget() {
        return target;
    }

    public boolean isTargetUsableAsWhitelistEntry() {
        return isTargetUsableAsWhiteListEntry;
    }

}
