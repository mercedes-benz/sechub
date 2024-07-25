package mercedesbenz.com.sechub.archunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

class ArchUnitRuntimeSupport {

    private Map<String, String> buildSystemToBinaryFolder = Map.of("gradle", "/build/classes/", "intelliJ", "/out/", "eclipse", "/bin/");

    public List<ImportOption> createImportOptionsForBuildSystem() {
        String buildSystem = resolveBuildSystem();

        Map<String, String> binaryFolderToIgnoreMap = new HashMap<>(buildSystemToBinaryFolder);
        binaryFolderToIgnoreMap.remove(buildSystem);

        List<ImportOption> importOptions = new ArrayList<>();
        for (String binaryFolder : binaryFolderToIgnoreMap.values()) {
            importOptions.add(new ImportOption() {
                @Override
                public boolean includes(Location location) {
                    return !location.contains(binaryFolder);
                }
            });
        }

        return importOptions;
    }

    private String resolveBuildSystem() {
        String buildSystem = System.getProperty("sechub.archunit.buildsystem");
        if (buildSystem == null || buildSystem.isBlank()) {
            buildSystem = "gradle";
        }

        switch (buildSystem) {
        case "gradle":
        case "intelliJ":
        case "eclipse":
            return buildSystem;
        default:
            throw new IllegalStateException("Unsupported build system: " + buildSystem);
        }
    }
}
