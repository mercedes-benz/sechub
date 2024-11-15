// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.mercedesbenz.sechub.zapwrapper.cli.ZapWrapperRuntimeException;

class ZapPDSEventHandlerTest {

    @Test
    void file_does_not_exist_and_so_no_scan_is_cancelled() throws IOException {
        /* prepare */
        String scanContextName = UUID.randomUUID().toString();
        ZapPDSEventHandler zapPDSEventHandler = new ZapPDSEventHandler("");

        /* execute + test */
        assertFalse(zapPDSEventHandler.isScanCancelled());
        assertDoesNotThrow(() -> zapPDSEventHandler.cancelScan(scanContextName));
    }

    @Test
    void file_does_exist_and_so_scan_is_cancelled(@TempDir Path tempDir) throws IOException {
        /* prepare */
        Path tempFile = Files.createFile(tempDir.resolve("cancel_requested.json"));
        ZapPDSEventHandler zapPDSEventHandler = new ZapPDSEventHandler(tempFile.getParent().toString());
        String scanContextName = UUID.randomUUID().toString();

        /* execute + test */
        assertTrue(zapPDSEventHandler.isScanCancelled());
        assertThrows(ZapWrapperRuntimeException.class, () -> zapPDSEventHandler.cancelScan(scanContextName));
    }

}
