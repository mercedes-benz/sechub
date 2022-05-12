package com.mercedesbenz.sechub.integrationtest.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MockedAdapterSetupAccessTest {

    @Test
    void smoke_test() {
        assertNotNull(MockedAdapterSetupAccess.get().getSetup());
    }

}
