// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.io.IOException;

/**
 * This exception is thrown when an error occurs during extraction of an archive through the
 * {@link SafeArchiveInputStream}.
 *
 * @author hamidonos
 */
public class ArchiveExtractionException extends IOException {
    private static final long serialVersionUID = 1L;

    public ArchiveExtractionException(String msg) {
        super(msg);
    }
}
