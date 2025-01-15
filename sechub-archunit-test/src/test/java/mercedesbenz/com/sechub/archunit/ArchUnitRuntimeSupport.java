// SPDX-License-Identifier: MIT
package mercedesbenz.com.sechub.archunit;

import java.util.*;

import com.tngtech.archunit.core.importer.ImportOption;

class ArchUnitRuntimeSupport {

    static final String SECHUB_ARCHUNIT_IGNORE_FOLDERS = "sechub.archunit.ignoreFolders";

    public List<ImportOption> createImportOptionsIgnoreFolder() {
        List<ImportOption> importOptions = new ArrayList<>();
        String defaultIgnoreFolder = "bin,out";

        /*
         * comma seperated list of folders to ignore e.g. build folders from different
         * builds
         */
        String folderToIgnore = System.getProperty(SECHUB_ARCHUNIT_IGNORE_FOLDERS);
        if (folderToIgnore == null || folderToIgnore.isBlank()) {
            System.setProperty(SECHUB_ARCHUNIT_IGNORE_FOLDERS, defaultIgnoreFolder);
            folderToIgnore = defaultIgnoreFolder;
        }

        folderToIgnore = folderToIgnore.trim();
        String[] folders = folderToIgnore.split(",");
        for (String folder : folders) {
            importOptions.add(location -> !location.contains(folder));
        }
        return importOptions;
    }

}