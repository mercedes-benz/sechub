// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.systemtest.config.TestDefinition;
import com.mercedesbenz.sechub.systemtest.runtime.launch.ProcessContainer;

public class LocationSupport {

    private static final Logger LOG = LoggerFactory.getLogger(LocationSupport.class);

    private Path pdsSolutionsRoot;
    private Path sechubSolutionRoot;
    private Path workspaceRoot;

    private Path additionalResourcesRoot;

    public static LocationSupportBuilder builder() {
        return new LocationSupportBuilder();
    }

    public static class LocationSupportBuilder {
        private String pdsSolutionsRootFolder;
        private String sechubSolutionRootFolder;
        private String workspaceRootFolder;
        private String additionalResourcesFolder;

        private LocationSupportBuilder() {

        }

        public LocationSupportBuilder pdsSolutionsRootFolder(String pdsSolutionsRootFolder) {
            this.pdsSolutionsRootFolder = pdsSolutionsRootFolder;
            return this;
        }

        public LocationSupportBuilder sechubSolutionRootFolder(String sechubSolutionRootFolder) {
            this.sechubSolutionRootFolder = sechubSolutionRootFolder;
            return this;
        }

        public LocationSupportBuilder workspaceRootFolder(String workspaceRootFolder) {
            this.workspaceRootFolder = workspaceRootFolder;
            return this;
        }

        public LocationSupportBuilder additionalResourcesFolder(String additionalResourcesFolder) {
            this.additionalResourcesFolder = additionalResourcesFolder;
            return this;
        }

        public LocationSupport build() {
            LocationSupport support = new LocationSupport();

            initPDSSolutionRootFolder(support);
            initSecHubSolutionRootFolder(support);
            initWorkspaceRootFolder(support);
            initAdditionalResourcesFolder(support);

            return support;
        }

        private void initAdditionalResourcesFolder(LocationSupport support) {
            if (additionalResourcesFolder != null) {
                try {
                    support.additionalResourcesRoot = Paths.get(additionalResourcesFolder).toAbsolutePath().toRealPath();
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot determine real path for additional resources folder: " + additionalResourcesFolder, e);
                }
            } else {
                support.additionalResourcesRoot = new File("./").toPath();
            }
            LOG.debug("Additional resource folder:{}", support.additionalResourcesRoot);
        }

        private void initWorkspaceRootFolder(LocationSupport support) {
            if (workspaceRootFolder != null) {
                try {
                    support.workspaceRoot = Paths.get(workspaceRootFolder).toAbsolutePath().toRealPath();
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot determine real path for wokspace root folder: " + workspaceRootFolder, e);
                }
            } else {
                try {
                    support.workspaceRoot = Files.createTempDirectory("systemtest_workspace");
                } catch (IOException e) {
                    throw new SystemTestRuntimeException("Cannot create workspace root", e);
                }
            }
            LOG.debug("Workspace root:{}", support.workspaceRoot);
        }

        private void initSecHubSolutionRootFolder(LocationSupport support) {
            if (sechubSolutionRootFolder != null) {
                try {
                    support.sechubSolutionRoot = Paths.get(sechubSolutionRootFolder).toAbsolutePath().toRealPath();
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot determine real path for pds solutions root folder: " + pdsSolutionsRootFolder, e);
                }
            } else {
                support.sechubSolutionRoot = support.pdsSolutionsRoot.getParent().resolve("sechub-solution");
            }
            LOG.debug("SecHub solution root:{}", support.sechubSolutionRoot);
        }

        private void initPDSSolutionRootFolder(LocationSupport support) {
            if (pdsSolutionsRootFolder == null) {
                try {
                    pdsSolutionsRootFolder = Files.createTempDirectory("systemtest_pds_solution_rootfolder_fallback").toString();
                } catch (IOException e) {
                    throw new SystemTestRuntimeException("Cannot create pds solution rootfolder fallback", e);
                }
            }
            try {
                support.pdsSolutionsRoot = Paths.get(pdsSolutionsRootFolder).toAbsolutePath().toRealPath();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot determine real path for " + pdsSolutionsRootFolder, e);
            }
            LOG.debug("PDS solution root:{}", support.pdsSolutionsRoot);
        }
    }

    private LocationSupport() {

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

    public Path getAdditionalResourcesRoot() {
        return additionalResourcesRoot;
    }

    public Path ensureOutputFile(ProcessContainer processContainer) {
        String processContainerFilenamePart = createFilenamePartFor(processContainer);
        return ensureProcessRuntimeFileRealPath(processContainer, "output_" + processContainerFilenamePart + ".txt");
    }

    public Path ensureErrorFile(ProcessContainer processContainer) {
        String processContainerFilenamePart = createFilenamePartFor(processContainer);
        return ensureProcessRuntimeFileRealPath(processContainer, "error_" + processContainerFilenamePart + ".txt");
    }

    public Path ensureProcessContainerErrorFile(ProcessContainer processContainer) {
        String processContainerFilenamePart = createFilenamePartFor(processContainer);
        return ensureProcessRuntimeFileRealPath(processContainer, "process-container-error_" + processContainerFilenamePart + ".txt");
    }

    public Path ensureProcessContainerFile(ProcessContainer processContainer) {
        String processContainerFilenamePart = createFilenamePartFor(processContainer);
        return ensureProcessRuntimeFileRealPath(processContainer, "process-container_" + processContainerFilenamePart + ".json");
    }

    /**
     * Resolves runtime folder - but it is not ensured that this folder really
     * exists!
     *
     * @return runtime folder
     */
    public Path getRuntimeFolder() {
        return workspaceRoot.resolve("runtime");
    }

    private Path ensureProcessRuntimeFileRealPath(ProcessContainer processContainer, String fileName) {
        Path processFolder = ensureProcessFolderRealPath(processContainer);
        Path processRuntimeFile = processFolder.resolve(fileName);
        return assertFileAndReturnRealPath(processRuntimeFile);
    }

    private Path ensureProcessFolderRealPath(ProcessContainer processContainer) {
        Path runtimeFolder = ensureRuntimeFolderRealPath();

        String folderName = createFilenamePartFor(processContainer);
        Path processFolder = runtimeFolder.resolve("process-containers").resolve(folderName);
        return ensureFolderAndReturnRealPath(processFolder);
    }

    private String createFilenamePartFor(ProcessContainer processContainer) {
        return processContainer.getNumber() + "_" + processContainer.getTargetFile().getName() + "_";
    }

    public Path ensureRuntimeFolderRealPath() {
        Path runtimeFolder = getRuntimeFolder();
        return ensureFolderAndReturnRealPath(runtimeFolder);
    }

    public Path ensureRuntimeArtifactsFolderRealPath(String testName) {
        Path runtimeArtifactsFolder = getRuntimeFolder().resolve("artifacts").resolve(testName);
        return ensureFolderAndReturnRealPath(runtimeArtifactsFolder);
    }

    public Path ensureTestWorkingDirectoryRealPath(TestDefinition test) {
        String testName = test.getName();
        if (testName == null) {
            throw new IllegalStateException("test name is null - may not happen!");
        }
        Path tests = getWorkspaceTestsFolder();
        Path testFolder = tests.resolve(testName);
        return ensureFolderAndReturnRealPath(testFolder);
    }

    private Path getWorkspaceTestsFolder() {
        Path workspaceRoot = getWorkspaceRoot();
        Path tests = workspaceRoot.resolve("tests");
        return ensureFolderAndReturnRealPath(tests);
    }

    private Path assertFileAndReturnRealPath(Path file) {
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException e) {
                throw new SystemTestRuntimeException("Was not able to create:" + file, e);
            }
        }
        return convertFileToRealPath(file);
    }

    private Path convertFileToRealPath(Path file) {
        try {
            return file.toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException("Was not able to ensure real path for file: " + file, e);
        }
    }

    private Path ensureFolderAndReturnRealPath(Path folder) {
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                throw new SystemTestRuntimeException("Was not able to create folder:" + folder, e);
            }
        }
        return convertFolderToRealPath(folder);
    }

    private Path convertFolderToRealPath(Path folder) {
        try {
            return folder.toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException("Was not able to ensure real path for folder: " + folder, e);
        }
    }

}
