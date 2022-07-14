package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.InputStream;
import java.util.Set;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProvider;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProviderFactory;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.checkmarx.CheckmarxWrapperEnvironment;

public class CheckmarxWrapperContext {

    private SecHubConfigurationModel configuration;
    private CheckmarxWrapperEnvironment environment;
    private NamePatternIdProvider presetIdProvider;
    private NamePatternIdProvider teamIdProvider;

    CheckmarxWrapperContext(SecHubConfigurationModel configuration, CheckmarxWrapperEnvironment environment, NamePatternIdProviderFactory factory) {
        this.configuration = configuration;
        this.environment = environment;

        presetIdProvider = factory.createProvider(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID, environment.getNewProjectPresetIdMapping());
        teamIdProvider = factory.createProvider(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID, environment.getNewProjectTeamIdMapping());
    }

    public Set<String> createCodeUploadFileSystemFolders() {
        return null;
    }

    public InputStream createSourceCodeZipFileInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    public CheckmarxWrapperEnvironment getEnvironment() {
        return environment;
    }

    public SecHubConfigurationModel getConfiguration() {
        return configuration;
    }

    public String getTeamIdForNewProjects() {
        return teamIdProvider.getIdForName(getProjectId());
    }

    public Long getPresetIdForNewProjects() {
        return Long.valueOf(presetIdProvider.getIdForName(getProjectId()));
    }

    public String getProjectId() {
        return configuration.getProjectId();
    }
}
