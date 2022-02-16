// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import com.daimler.sechub.integrationtest.api.TestUser;

public class IntegrationTestRestHelper extends TestRestHelper {

    public IntegrationTestRestHelper(TestUser user, RestHelperTarget target) {
        super(user, target);
    }

}
