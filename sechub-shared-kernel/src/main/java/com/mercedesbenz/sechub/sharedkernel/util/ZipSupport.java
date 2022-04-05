// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZipSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ZipSupport.class);

    public boolean isZipFile(InputStream inputStream) {
        if (inputStream == null) {
            return false;
        }

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            boolean isZipped = zis.getNextEntry() != null;
            return isZipped;
        } catch (IOException e) {
            // only interesting for debugging - normally it is just no ZIP file.
            LOG.debug("The zip file check did fail", e);
            return false;
        }
    }

}
