package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocationSupport {

    private Path pdsSolutionRootPath;
    private Path sechubSolutionRootFolderPath;

    public LocationSupport(String pdsSolutionsRootFolder, String sechubSolutionRootFolder) {
        try {
            pdsSolutionRootPath = Paths.get(pdsSolutionsRootFolder).toAbsolutePath().toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot determine real path for " + pdsSolutionsRootFolder, e);
        }
        if (sechubSolutionRootFolder != null) {
            try {
                sechubSolutionRootFolderPath = Paths.get(pdsSolutionsRootFolder).toAbsolutePath().toRealPath();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot determine real path for " + sechubSolutionRootFolderPath, e);
            }
        } else {
            sechubSolutionRootFolderPath = pdsSolutionRootPath.getParent().resolve("sechub-solution");
        }
    }

    public Path getPDSSolutionRootFolder() {
        return pdsSolutionRootPath;
    }

    public Path getSecHubSolutionRootFolder() {
        return sechubSolutionRootFolderPath;
    }
}
