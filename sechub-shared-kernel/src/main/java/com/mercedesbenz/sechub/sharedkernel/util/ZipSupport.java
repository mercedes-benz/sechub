// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Component;

@Component
public class ZipSupport {

    public boolean isZipFile(InputStream inputStream) {
        if (inputStream == null) {
            return false;
        }

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            boolean isZipped = zis.getNextEntry() != null;
            return isZipped;
        } catch (IOException e) {
            return false;
        }
    }

}
