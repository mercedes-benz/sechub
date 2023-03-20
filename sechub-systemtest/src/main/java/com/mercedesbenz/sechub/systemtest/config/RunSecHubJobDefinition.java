package com.mercedesbenz.sechub.systemtest.config;

public class RunSecHubJobDefinition extends AbstractDefinition {

    private String project;

    private UploadDefinition upload = new UploadDefinition();

    public UploadDefinition getUpload() {
        return upload;
    }

    public void setProject(String projectName) {
        this.project = projectName;
    }

    public String getProject() {
        return project;
    }

}
