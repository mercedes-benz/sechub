// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HTTPHeaderConfigurationTest {

    @Test
    void new_http_header_configuration_instance_is_sensitive_on_default() {
        /* execute */
        HTTPHeaderConfiguration emptyHeaderConfig = new HTTPHeaderConfiguration();

        /* test */
        assertEquals(true, emptyHeaderConfig.isSensitive());
    }

}
