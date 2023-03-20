package com.mercedesbenz.sechub.systemtest.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScriptDefinition extends AbstractDefinition {

    private Map<String, String> envVariables = new LinkedHashMap<>();
    private String path;
    private String workingDirectory;

    public Map<String, String> getEnvVariables() {
        return envVariables;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setWorkingDir(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }
}
