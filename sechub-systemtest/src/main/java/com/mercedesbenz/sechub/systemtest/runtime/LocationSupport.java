package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocationSupport {

    private Path pdsSolutionsRoot;
    private Path sechubSolutionRoot;
    private Path workspaceRoot;

    public LocationSupport(String pdsSolutionsRootFolder, String sechubSolutionRootFolder, String workspaceRootFolder) {
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

        if (workspaceRootFolder != null) {
            try {
                workspaceRoot = Paths.get(workspaceRootFolder).toAbsolutePath().toRealPath();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot determine real path for " + sechubSolutionRoot, e);
            }
        } else {
            try {
                workspaceRoot = Files.createTempDirectory("systemteset_workspace");
            } catch (IOException e) {
                throw new SystemTestRuntimeException("Cannot create workspace root", e);
            }
        }
    }

    public Path getPDSSolutionRoot() {
        return pdsSolutionsRoot;
    }

    public Path getSecHubSolutionRoot() {
        return sechubSolutionRoot;
    }

    public Path getWorkspaceRoot() {
        return workspaceRoot;
    }
}
