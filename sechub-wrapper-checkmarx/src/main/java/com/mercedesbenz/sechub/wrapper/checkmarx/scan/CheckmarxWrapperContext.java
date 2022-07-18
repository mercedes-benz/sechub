package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.InputStream;
import java.util.Set;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProvider;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProviderFactory;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperCLIEnvironment;

public class CheckmarxWrapperContext {

    private SecHubConfigurationModel configuration;
    private CheckmarxWrapperCLIEnvironment environment;
    private NamePatternIdProvider presetIdProvider;
    private NamePatternIdProvider teamIdProvider;

    CheckmarxWrapperContext(SecHubConfigurationModel configuration, CheckmarxWrapperCLIEnvironment environment, NamePatternIdProviderFactory factory) {
        this.configuration = configuration;
        this.environment = environment;

        String newProjectPresetIdMappingDataAsJson = environment.getNewProjectPresetIdMapping();
        String newProjectTeamIdMappingDataAsJson = environment.getNewProjectTeamIdMapping();

        presetIdProvider = factory.createProvider(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID, newProjectPresetIdMappingDataAsJson);
        teamIdProvider = factory.createProvider(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID, newProjectTeamIdMappingDataAsJson);
    }

    public Set<String> createCodeUploadFileSystemFolders() {
        return null;
    }

    public InputStream createSourceCodeZipFileInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    public CheckmarxWrapperCLIEnvironment getEnvironment() {
        return environment;
    }

    public SecHubConfigurationModel getConfiguration() {
        return configuration;
    }

    public String getTeamIdForNewProjects() {
        String projectId = getProjectId();
        String teamId = teamIdProvider.getIdForName(projectId);
        if (teamId == null) {
            throw new IllegalStateException("Was not able to determine the team id for project: " + projectId);
        }
        return teamId;
    }

    public Long getPresetIdForNewProjects() {
        String projectId = getProjectId();

        String presetId = presetIdProvider.getIdForName(projectId);
        if (presetId == null) {
            throw new IllegalStateException("Was not able to determine the preset id for project: " + projectId);
        }
        return Long.valueOf(presetId);
    }

    public String getProjectId() {
        String projectId = configuration.getProjectId();
        if (projectId == null) {
            throw new IllegalStateException("Project id is missing!");
        }
        return projectId;
    }
}
