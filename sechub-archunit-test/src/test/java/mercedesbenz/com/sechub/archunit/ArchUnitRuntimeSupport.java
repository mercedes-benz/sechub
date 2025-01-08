// SPDX-License-Identifier: MIT
package mercedesbenz.com.sechub.archunit;

import java.util.*;

import com.tngtech.archunit.core.importer.ImportOption;

class ArchUnitRuntimeSupport {

    public static final String ARCHUNIT_SUPPORT_NOTE = """
            An archunit test has failed.
            Please check if you violated the defined rules in your implementation.
            If not: clean and rebuild, as archunit works on the build.
            If you receive a duplicated error (e.g. Multiple entries with same key), you can use the following system property to ignore specific folders build e.g.:
            -Dsechub.archunit.ignoreFolders=bin,out
            -- will ignore eclipse and intelliJ builds and only scan your gradle /build folder --
            """;

    public List<ImportOption> createImportOptionsIgnoreFolder() {
        List<ImportOption> importOptions = new ArrayList<>();

        // comma seperated list of folders to ignore e.g. build folders from different
        // builds
        String folderToIgnore = System.getProperty("sechub.archunit.ignoreFolders");
        if (folderToIgnore == null || folderToIgnore.isBlank()) {
            return importOptions;
        }

        folderToIgnore = folderToIgnore.trim();
        String[] folders = folderToIgnore.split(",");
        for (String folder : folders) {
            importOptions.add(location -> !location.contains(folder));
        }
        return importOptions;
    }

}