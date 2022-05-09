// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.util;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.archive.ZipSupport;

@Component
public class PDSArchiveSupportProvider {
    ZipSupport zipSupport = new ZipSupport();

    public ZipSupport getZipSupport() {
        return zipSupport;
    }

}
