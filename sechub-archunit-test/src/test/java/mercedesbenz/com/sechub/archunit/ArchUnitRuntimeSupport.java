package mercedesbenz.com.sechub.archunit;

import java.util.*;

import com.tngtech.archunit.core.importer.ImportOption;

class ArchUnitRuntimeSupport {

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