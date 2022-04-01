// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SystemTimeProviderTest {

    @Test
    void get_now_does_not_return_null() {
        assertNotNull(new SystemTimeProvider().getNow());
    }

}
