package com.mercedesbenz.sechub.systemtest.runtime;

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

    public LocationSupport(String pdsSolutionsRootFolder, String sechubSolutionRootFolder, String workspaceRootFolder) {
        if (pdsSolutionsRootFolder == null) {
            try {
                pdsSolutionsRootFolder = Files.createTempDirectory("systemtest_pds_solution_rootfolder_fallback").toString();
            } catch (IOException e) {
                throw new SystemTestRuntimeException("Cannot create pds solution rootfolder fallback", e);
            }
        }
        try {
            pdsSolutionsRoot = Paths.get(pdsSolutionsRootFolder).toAbsolutePath().toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot determine real path for " + pdsSolutionsRootFolder, e);
        }
        LOG.debug("PDS solution root:{}", pdsSolutionsRoot);
        if (sechubSolutionRootFolder != null) {
            try {
                sechubSolutionRoot = Paths.get(pdsSolutionsRootFolder).toAbsolutePath().toRealPath();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot determine real path for " + sechubSolutionRoot, e);
            }
        } else {
            sechubSolutionRoot = pdsSolutionsRoot.getParent().resolve("sechub-solution");
        }
        LOG.debug("SecHub solution root:{}", sechubSolutionRoot);

        if (workspaceRootFolder != null) {
            try {
                workspaceRoot = Paths.get(workspaceRootFolder).toAbsolutePath().toRealPath();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot determine real path for " + sechubSolutionRoot, e);
            }
        } else {
            try {
                workspaceRoot = Files.createTempDirectory("systemtest_workspace");
            } catch (IOException e) {
                throw new SystemTestRuntimeException("Cannot create workspace root", e);
            }
        }
        LOG.debug("Workspace root:{}", workspaceRoot);
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

    public Path ensureOutputFile(ProcessContainer processContainer) {
        return ensureProcessRuntimeFile(processContainer, "output.txt");
    }

    public Path ensureErrorFile(ProcessContainer processContainer) {
        return ensureProcessRuntimeFile(processContainer, "error.txt");
    }

    public Path ensureProcessContainerFile(ProcessContainer processContainer) {
        return ensureProcessRuntimeFile(processContainer, "process-container.json");
    }

    /**
     * Resolves runtime folder - but it is not ensured that this folder really
     * exists!
     *
     * @return runtime folder
     */
    public Path getRuntimeFolder() {
        return workspaceRoot.resolve(".runtime");
    }

    private Path ensureProcessRuntimeFile(ProcessContainer processContainer, String fileName) {
        Path processFolder = ensureProcessFolder(processContainer);
        Path processRuntimeFile = processFolder.resolve(fileName);
        if (!Files.exists(processRuntimeFile)) {
            try {
                Files.createFile(processRuntimeFile);
            } catch (IOException e) {
                throw new SystemTestRuntimeException("Was not able to create process runtime file:" + processRuntimeFile, e);
            }
        }
        return processRuntimeFile;
    }

    private Path ensureProcessFolder(ProcessContainer processContainer) {
        Path runtimeFolder = ensureRuntimeFolder();
        Path processFolder = runtimeFolder.resolve("process-container#"+processContainer.getNumber()+"["+processContainer.getUuid().toString()+"]");
        if (!Files.exists(processFolder)) {
            try {
                Files.createDirectories(processFolder);
            } catch (IOException e) {
                throw new SystemTestRuntimeException("Was not able to create process folder:" + processFolder, e);
            }
        }
        return processFolder;
    }

    private Path ensureRuntimeFolder() {
        Path runtimeFolder = getRuntimeFolder();
        try {
            if (!Files.exists(runtimeFolder)) {
                Files.createDirectories(runtimeFolder);
            }
            return runtimeFolder.toRealPath();
        } catch (IOException e) {
            throw new SystemTestRuntimeException("Was not able to create runtime folder:" + runtimeFolder, e);
        }
    }

    public Path ensureTestFolder(TestDefinition test) {
        String testName = test.getName();
        if (testName == null) {
            throw new IllegalStateException("test name is null- may not happen!");
        }
        Path tests = getWorkspaceTestsFolder();
        Path testFolder = tests.resolve(testName);
        try {
            Files.createDirectories(testFolder);
            return testFolder.toRealPath();

        } catch (IOException e) {
            throw new SystemTestRuntimeException("Not able to ensure test folder for test: " + testName + " inside workspace root: " + workspaceRoot, e);
        }

    }

    private Path getWorkspaceTestsFolder() {
        Path workspaceRoot = getWorkspaceRoot();
        Path tests = workspaceRoot.resolve("tests");

        if (!Files.exists(tests)) {
            try {
                Files.createDirectories(tests);
            } catch (IOException e) {
                throw new SystemTestRuntimeException("Not able to ensure tests folder inside workspace root: " + workspaceRoot, e);
            }
        }
        return tests;
    }

}
