package com.mercedesbenz.sechub.domain.scan.template;

import java.util.Set;
import java.util.TreeSet;

public class TemplateHealthCheckEntry {

    private TemplateHealthCheckProblemType type;
    private String description;
    private String templateId;
    private Set<String> projects = new TreeSet<>();
    private String executorConfigUUID;
    private Set<String> profiles = new TreeSet<>();

    private Set<String> hints = new TreeSet<>();
    private String solution;

    private String assetId;
    private String fileName;

    public TemplateHealthCheckProblemType getType() {
        return type;
    }

    public void setType(TemplateHealthCheckProblemType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String template) {
        this.templateId = template;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String asset) {
        this.assetId = asset;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Set<String> getProfiles() {
        return profiles;
    }

    public String getExecutorConfigUUID() {
        return executorConfigUUID;
    }

    public void setExecutorConfigUUID(String executorConfiguration) {
        this.executorConfigUUID = executorConfiguration;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String suggestion) {
        this.solution = suggestion;
    }

    public Set<String> getProjects() {
        return projects;
    }

    public Set<String> getHints() {
        return hints;
    }

}
