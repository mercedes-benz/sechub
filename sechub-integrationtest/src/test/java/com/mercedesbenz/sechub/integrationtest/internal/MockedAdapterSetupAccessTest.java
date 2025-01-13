// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.integrationtest.api.TestOnlyForRegularExecution;

@TestOnlyForRegularExecution
class MockedAdapterSetupAccessTest {

    @Test
    void smoke_test() {
        assertNotNull(MockedAdapterSetupAccess.get().getSetup());
    }

}
