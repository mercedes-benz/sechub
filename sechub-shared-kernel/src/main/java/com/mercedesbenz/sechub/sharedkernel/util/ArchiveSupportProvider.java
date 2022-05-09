// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.util;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.archive.ZipSupport;

@Component
public class ArchiveSupportProvider {

    ZipSupport zipSupport = new ZipSupport();

    public ZipSupport getZipSupport() {
        return zipSupport;
    }

}
