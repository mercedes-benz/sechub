package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.adapter.checkmarx.CheckmarxConstants;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProviderFactory;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperCLIEnvironment;

@Component
public class CheckmarxWrapperContextFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxWrapperContextFactory.class);

    @Autowired
    NamePatternIdProviderFactory providerFactory;

    @Autowired
    ArchiveSupport archiveSupport;

    @Autowired
    CodeScanPathCollector codeScanPathCollector;

    public CheckmarxWrapperContext create(CheckmarxWrapperCLIEnvironment environment) {

        SecHubConfigurationModel configuration = createModel(environment.getSechubConfigurationModelAsJson());

        CheckmarxWrapperContext result = new CheckmarxWrapperContext();
        result.configuration = configuration;
        result.environment = environment;

        String newProjectPresetIdMappingDataAsJson = environment.getNewProjectPresetIdMapping();
        String newProjectTeamIdMappingDataAsJson = environment.getNewProjectTeamIdMapping();

        result.presetIdProvider = providerFactory.createProvider(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_PRESET_ID,
                newProjectPresetIdMappingDataAsJson);
        result.teamIdProvider = providerFactory.createProvider(CheckmarxConstants.MAPPING_CHECKMARX_NEWPROJECT_TEAM_ID, newProjectTeamIdMappingDataAsJson);
        result.archiveSupport = archiveSupport;
        result.codeScanPathCollector = codeScanPathCollector;

        return result;
    }

    private SecHubConfigurationModel createModel(String json) {
        if (json == null || json.isEmpty()) {
            throw new IllegalStateException("No SecHub model JSON found, cannot create model");
        }
        try {
            return JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);

        } catch (JSONConverterException e) {
            LOG.error("Cannot convert given sechub configuration model", e);
        }
        LOG.warn("Because not being able to build correct sechub configuration model an empty model will be used as a fallback!");
        return new SecHubConfigurationModel(); // fallback - empty model
    }

}
