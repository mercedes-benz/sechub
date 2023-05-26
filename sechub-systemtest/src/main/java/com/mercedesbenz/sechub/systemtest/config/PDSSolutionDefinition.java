package com.mercedesbenz.sechub.systemtest.config;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PDSSolutionDefinition extends AbstractDefinition {

    private String name;
    private URL url;

    private String baseDir;
    private String pathToPdsServerConfigFile;

    private CredentialsDefinition techUser = new CredentialsDefinition();

    private List<ExecutionStepDefinition> start = new ArrayList<>();

    private Optional<Boolean> waitForAvailable = Optional.ofNullable(null);

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

    public void setWaitForAvailable(Optional<Boolean> waitForAvailable) {
        this.waitForAvailable = waitForAvailable;
    }

    public Optional<Boolean> getWaitForAvailable() {
        return waitForAvailable;
    }

    public List<ExecutionStepDefinition> getStart() {
        return start;
    }

    public List<ExecutionStepDefinition> getStop() {
        return stop;
    }

    /**
     * Set the base directory for the solution. Can contain variables which will be
     * resolved. This is optional. If not defined, the test runtime will try to
     * resolve it automatically (by name and defined folder location of
     * "sechub-pds-solutions". The PDS base directory is important, because the PDS
     * configuration file must be loaded to determine the provided products inside
     * the PDS.
     *
     * @param baseDir
     */
    public void setBaseDirectory(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public CredentialsDefinition getTechUser() {
        return techUser;
    }
}
