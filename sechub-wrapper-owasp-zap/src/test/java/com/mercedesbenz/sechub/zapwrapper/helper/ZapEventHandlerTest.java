// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

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

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

class ZapEventHandlerTest {

    private ZapEventHandler zapEventHandler;

    @BeforeEach
    void beforeEach() {
        zapEventHandler = new ZapEventHandler("");
    }

    @Test
    void file_does_not_exist_and_so_no_scan_is_cancelled() throws IOException {
        /* prepare */
        String scanContextName = UUID.randomUUID().toString();

        /* execute + test */
        assertFalse(zapEventHandler.isScanCancelled());
        assertDoesNotThrow(() -> zapEventHandler.cancelScan(scanContextName));
    }

    @Test
    void file_does_exist_and_so_scan_is_cancelled(@TempDir File tempDir) throws IOException {
        /* prepare */
        zapEventHandler.cancelEventFile = tempDir;
        String scanContextName = UUID.randomUUID().toString();

        /* execute + test */
        assertTrue(zapEventHandler.isScanCancelled());
        assertThrows(ZapWrapperRuntimeException.class, () -> zapEventHandler.cancelScan(scanContextName));
    }

}
