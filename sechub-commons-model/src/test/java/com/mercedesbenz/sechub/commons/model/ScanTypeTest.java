// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ScanTypeTest {
    @Test
    void secret_scan__get_id() {
        assertEquals("secretScan", ScanType.SECRET_SCAN.getId());
    }
}
