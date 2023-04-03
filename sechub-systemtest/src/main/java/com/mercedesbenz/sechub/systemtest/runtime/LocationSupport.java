package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocationSupport {

    private Path pdsSolutionsRoot;
    private Path sechubSolutionRoot;

    public LocationSupport(String pdsSolutionsRootFolder, String sechubSolutionRootFolder) {
        try {
            pdsSolutionsRoot = Paths.get(pdsSolutionsRootFolder).toAbsolutePath().toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot determine real path for " + pdsSolutionsRootFolder, e);
        }
        if (sechubSolutionRootFolder != null) {
            try {
                sechubSolutionRoot = Paths.get(pdsSolutionsRootFolder).toAbsolutePath().toRealPath();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot determine real path for " + sechubSolutionRoot, e);
            }
        } else {
            sechubSolutionRoot = pdsSolutionsRoot.getParent().resolve("sechub-solution");
        }
    }

    public Path getPDSSolutionRoot() {
        return pdsSolutionsRoot;
    }

    public Path getSecHubSolutionRoot() {
        return sechubSolutionRoot;
    }
}
