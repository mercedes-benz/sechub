// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mercedesbenz.sechub.owaspzapwrapper.cli.ZapWrapperRuntimeException;

class OwaspZapEventHandlerTest {

    private OwaspZapEventHandler owaspZapEventHandler;

    @BeforeEach
    void beforeEach() {
        owaspZapEventHandler = new OwaspZapEventHandler();
    }

    @Test
    void file_does_not_exist_and_so_no_scan_is_cancelled() throws IOException {
        /* prepare */
        String scanContextName = UUID.randomUUID().toString();

        /* execute + test */
        assertFalse(owaspZapEventHandler.isScanCancelled());
        assertDoesNotThrow(() -> owaspZapEventHandler.cancelScan(scanContextName));
    }

    @Test
    void file_does_exist_and_so_scan_is_cancelled(@TempDir File tempDir) throws IOException {
        /* prepare */
        owaspZapEventHandler.cancelEventFile = tempDir;
        String scanContextName = UUID.randomUUID().toString();

        /* execute + test */
        assertTrue(owaspZapEventHandler.isScanCancelled());
        assertThrows(ZapWrapperRuntimeException.class, () -> owaspZapEventHandler.cancelScan(scanContextName));
    }

}
