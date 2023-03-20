package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.List;

public class PDSSolutionDefinition extends AbstractDefinition {

    private String name;

    private String baseDir;

    private String pathToPdsServerConfigFile;

    private List<ExecutionStepDefinition> start = new ArrayList<>();

    private boolean waitForPDSAvailable = true;

    private List<ExecutionStepDefinition> stop = new ArrayList<>();

    public String getPathToPdsServerConfigFile() {
        return pathToPdsServerConfigFile;
    }

    public void setPathToPdsServerConfigFile(String pathToPdsServerConfigFile) {
        this.pathToPdsServerConfigFile = pathToPdsServerConfigFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWaitForPDSAvailable() {
        return waitForPDSAvailable;
    }

    public void setWaitForPDSAvailable(boolean waitForAvailable) {
        this.waitForPDSAvailable = waitForAvailable;
    }

    public List<ExecutionStepDefinition> getStart() {
        return start;
    }

    public List<ExecutionStepDefinition> getStop() {
        return stop;
    }

    /**
     * Set the base directory for the solution. This is optional. If not defined,
     * the test runtime will try to resolve it automatically (by name and defined
     * folder location of "sechub-pds-solutions". The PDS base directory is
     * important, because the PDS configuration file must be loaded to determine the
     * provided products inside the PDS.
     *
     * @param baseDir
     */
    public void setBaseDirectory(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getBaseDir() {
        return baseDir;
    }
}
