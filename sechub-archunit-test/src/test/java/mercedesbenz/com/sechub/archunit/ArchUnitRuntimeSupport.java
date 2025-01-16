// SPDX-License-Identifier: MIT
package mercedesbenz.com.sechub.archunit;

import java.util.*;

import com.tngtech.archunit.core.importer.ImportOption;

class ArchUnitRuntimeSupport {

    static String DEFAULT_IGNORED_FOLDERS = "bin,out";

    static final String SECHUB_ARCHUNIT_IGNORE_FOLDERS = "sechub.archunit.ignoreFolders";

    private static String foldersToIgnore;

    static {
        String sechubArchUnitIgnoreFolders = System.getProperty(SECHUB_ARCHUNIT_IGNORE_FOLDERS);
        if (sechubArchUnitIgnoreFolders == null || sechubArchUnitIgnoreFolders.isBlank()) {
            System.setProperty(SECHUB_ARCHUNIT_IGNORE_FOLDERS, DEFAULT_IGNORED_FOLDERS);
            sechubArchUnitIgnoreFolders = DEFAULT_IGNORED_FOLDERS;
        }
        sechubArchUnitIgnoreFolders = sechubArchUnitIgnoreFolders.trim();

        foldersToIgnore = sechubArchUnitIgnoreFolders;
    }

    /**
     * @return comma separated list of folders to ignore e.g. build folders when
     *         having different build systems (e.g. eclipse build and gradle build
     *         together)
     */
    static String getFoldersToIgnore() {
        return foldersToIgnore;
    }

    public List<ImportOption> createImportOptionsIgnoreFolder() {
        List<ImportOption> importOptions = new ArrayList<>();

        String[] folders = getFoldersToIgnore().split(",");
        for (String folder : folders) {
            importOptions.add(location -> !location.contains(folder));
        }
        return importOptions;
    }

}