// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.util;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;

@Component
public class ArchiveSupportProvider {

    ArchiveSupport zipSupport = new ArchiveSupport();

    public ArchiveSupport getArchiveSupport() {
        return zipSupport;
    }

}
