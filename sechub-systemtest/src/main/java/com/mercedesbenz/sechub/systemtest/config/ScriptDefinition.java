// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScriptDefinition extends AbstractDefinition {

    private Map<String, String> envVariables = new LinkedHashMap<>();
    private String path;
    private String workingDirectory;
    private List<String> arguments = new ArrayList<>();
    private ProcessDefinition process = new ProcessDefinition();

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

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public ProcessDefinition getProcess() {
        return process;
    }

    public void setProcess(ProcessDefinition process) {
        this.process = process;
    }
}
