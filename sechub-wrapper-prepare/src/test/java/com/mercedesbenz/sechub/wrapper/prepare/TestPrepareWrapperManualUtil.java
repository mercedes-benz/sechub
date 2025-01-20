// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class TestPrepareWrapperManualUtil {

    public static void cleanup() throws IOException {
        File manuelTestFolder = new File("./build/manual-test/");
        cleanupFolder(manuelTestFolder);
    }

    private static void cleanupFolder(File folder) throws IOException {
        /* remove old data and ensure directory exists afterwards */

        if (folder.exists()) {
            FileUtils.forceDelete(folder);
        }
        FileUtils.forceMkdir(folder);

        System.out.println("Cleaned up folder: " + folder.getAbsolutePath());
    }
}
