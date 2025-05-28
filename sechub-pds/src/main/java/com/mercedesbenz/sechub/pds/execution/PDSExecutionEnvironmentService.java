// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static com.mercedesbenz.sechub.commons.pds.PDSLauncherScriptEnvironmentConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.pds.ExecutionPDSKey;
import com.mercedesbenz.sechub.commons.pds.PDSConfigDataKeyProvider;
import com.mercedesbenz.sechub.commons.pds.PDSLauncherScriptEnvironmentConstants;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.job.PDSJobConfiguration;
import com.mercedesbenz.sechub.pds.job.PDSWorkspaceService;
import com.mercedesbenz.sechub.pds.job.WorkspaceLocationData;

@Service
public class PDSExecutionEnvironmentService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionEnvironmentService.class);

    @Autowired
    PDSKeyToEnvConverter converter;

    @Autowired
    PDSServerConfigurationService serverConfigService;

    @Autowired
    PDSWorkspaceService workspaceService;

    @Autowired
    PDSScriptEnvironmentCleaner environmentCleaner;

    public void initProcessBuilderEnvironmentMap(UUID pdsJobUUID, PDSJobConfiguration config, ProcessBuilder builder) {
        /* first assert parameters and state */
        if (pdsJobUUID == null) {
            throw new IllegalArgumentException("pds job uuid may not be null!");
        }
        if (config == null) {
            throw new IllegalArgumentException("pds job configuration may not be null!");
        }
        if (builder == null) {
            throw new IllegalArgumentException("pds job configuration may not be null!");
        }
        String productId = config.getProductId();
        if (productId == null) {
            throw new IllegalStateException("Product id may never be null!");
        }
        PDSProductSetup productSetup = serverConfigService.getProductSetupOrNull(productId);
        if (productSetup == null) {
            throw new IllegalStateException("Product setup for product: " + productId + " may never be null!");
        }

        /* now calculate */
        calculateAndSetupEnvironment(pdsJobUUID, config, builder, productSetup);

    }

    private void calculateAndSetupEnvironment(UUID pdsJobUUID, PDSJobConfiguration config, ProcessBuilder builder, PDSProductSetup productSetup) {
        /* now init environment map */
        PDSSafeProcessEnvironmentAccess environment = initCleanEnvironment(productSetup, builder);

        addPdsJobRelatedVariables(pdsJobUUID, environment);
        addPdsExecutorJobParameters(productSetup, config, environment);
        addSecHubJobUUIDAsEnvironmentEntry(config, environment);

        replaceNullValuesWithEmptyStrings(environment);

        LOG.debug("Initialized environment variables for script of pds job: {} with: {}", pdsJobUUID, environment);
    }

    private PDSSafeProcessEnvironmentAccess initCleanEnvironment(PDSProductSetup productSetup, ProcessBuilder builder) {
        Map<String, String> environment = builder.environment();

        Set<String> pdsScriptEnvWhitelist = productSetup.getEnvWhitelist();
        LOG.debug("PDS script environment variable white list: '{}'", pdsScriptEnvWhitelist);

        environmentCleaner.clean(environment, pdsScriptEnvWhitelist);

        PDSSafeProcessEnvironmentAccess result = new PDSSafeProcessEnvironmentAccess(builder.environment());

        return result;

    }

    private void addPdsJobRelatedVariables(UUID pdsJobUUID, PDSSafeProcessEnvironmentAccess envAccess) {
        WorkspaceLocationData locationData = workspaceService.createLocationData(pdsJobUUID);

        envAccess.put(PDS_JOB_WORKSPACE_LOCATION, locationData.getWorkspaceLocation());
        envAccess.put(PDS_JOB_RESULT_FILE, locationData.getResultFileLocation());
        envAccess.put(PDS_JOB_USER_MESSAGES_FOLDER, locationData.getUserMessagesLocation());
        envAccess.put(PDS_JOB_EVENTS_FOLDER, locationData.getEventsLocation());
        envAccess.put(PDS_JOB_METADATA_FILE, locationData.getMetaDataFileLocation());
        envAccess.put(PDS_JOB_UUID, pdsJobUUID.toString());
        envAccess.put(PDS_JOB_SOURCECODE_ZIP_FILE, locationData.getSourceCodeZipFileLocation());
        envAccess.put(PDS_JOB_BINARIES_TAR_FILE, locationData.getBinariesTarFileLocation());

        String extractedAssetsLocation = locationData.getExtractedAssetsLocation();

        envAccess.put(PDS_JOB_EXTRACTED_ASSETS_FOLDER, extractedAssetsLocation);

        String extractedSourcesLocation = locationData.getExtractedSourcesLocation();

        envAccess.put(PDS_JOB_SOURCECODE_UNZIPPED_FOLDER, extractedSourcesLocation);
        envAccess.put(PDS_JOB_EXTRACTED_SOURCES_FOLDER, extractedSourcesLocation);

        String extractedBinariesLocation = locationData.getExtractedBinariesLocation();
        envAccess.put(PDS_JOB_EXTRACTED_BINARIES_FOLDER, extractedBinariesLocation);

        envAccess.put(PDS_JOB_HAS_EXTRACTED_SOURCES, "" + workspaceService.hasExtractedSources(pdsJobUUID));
        envAccess.put(PDS_JOB_HAS_EXTRACTED_BINARIES, "" + workspaceService.hasExtractedBinaries(pdsJobUUID));

    }

    private void addPdsExecutorJobParameters(PDSProductSetup productSetup, PDSJobConfiguration config, PDSSafeProcessEnvironmentAccess envAccess) {

        List<PDSExecutionParameterEntry> jobParams = config.getParameters();
        for (PDSExecutionParameterEntry jobParam : jobParams) {
            addJobParamDataWhenAccepted(productSetup, jobParam, envAccess);
        }

        addDefaultsForMissingParameters(productSetup, envAccess);

    }

    /*
     * Replace null values with empty strings to avoid problems with environment map
     * of process builder: This map does throw an exception in this case (index of
     * problems)
     */
    private void replaceNullValuesWithEmptyStrings(PDSSafeProcessEnvironmentAccess envAccess) {

        List<String> keysForEntriesWithNullValue = new ArrayList<>();

        for (String key : envAccess.getKeys()) {
            String value = envAccess.get(key);
            if (value == null) {
                keysForEntriesWithNullValue.add(key);
            }
        }

        for (String keyForEntryWithNullValue : keysForEntriesWithNullValue) {
            envAccess.put(keyForEntryWithNullValue, "");

            LOG.warn("Replaced null value for key: {} with empty string", keyForEntryWithNullValue);
        }

    }

    private void addDefaultsForMissingParameters(PDSProductSetup productSetup, PDSSafeProcessEnvironmentAccess envAccess) {
        PDSProductParameterSetup parameters = productSetup.getParameters();

        addDefaultsForMissingParametersInList(parameters.getMandatory(), envAccess);
        addDefaultsForMissingParametersInList(parameters.getOptional(), envAccess);
    }

    private void addDefaultsForMissingParametersInList(List<PDSProductParameterDefinition> parameterDefinitions, PDSSafeProcessEnvironmentAccess envAccess) {

        for (PDSProductParameterDefinition parameterDefinition : parameterDefinitions) {
            if (!parameterDefinition.hasDefault()) {
                continue;
            }
            String envVariableName = converter.convertKeyToEnv(parameterDefinition.getKey());

            String value = envAccess.get(envVariableName);

            if (value == null) {
                envAccess.put(envVariableName, parameterDefinition.getDefault());
            }
        }

    }

    private void addSecHubJobUUIDAsEnvironmentEntry(PDSJobConfiguration config, PDSSafeProcessEnvironmentAccess envAccess) {
        envAccess.put(PDSLauncherScriptEnvironmentConstants.SECHUB_JOB_UUID, fetchSecHubJobUUIDasString(config));
    }

    private String fetchSecHubJobUUIDasString(PDSJobConfiguration pdsJobConfiguration) {
        UUID sechubJobUUID = pdsJobConfiguration.getSechubJobUUID();
        if (sechubJobUUID == null) {
            LOG.error("No SecHub job UUID found, environment variable: {} will be empty", PDSLauncherScriptEnvironmentConstants.SECHUB_JOB_UUID);
            return "";
        }
        return sechubJobUUID.toString();
    }

    private void addJobParamDataWhenAccepted(PDSProductSetup productSetup, PDSExecutionParameterEntry jobParam, PDSSafeProcessEnvironmentAccess envAccess) {
        PDSProductParameterSetup params = productSetup.getParameters();

        boolean acceptedParameter = false;
        boolean wellknown = false;

        for (PDSConfigDataKeyProvider provider : PDSConfigDataKeyProvider.values()) {
            ExecutionPDSKey key = provider.getKey();
            if (!key.getId().equals(jobParam.getKey())) {
                continue;
            }
            wellknown = true;
            acceptedParameter = key.isAvailableInsideScript();
            break;
        }

        acceptedParameter = acceptedParameter || isJobParameterAcceptedByPDSServerConfiguration(jobParam, params.getMandatory());
        acceptedParameter = acceptedParameter || isJobParameterAcceptedByPDSServerConfiguration(jobParam, params.getOptional());

        if (acceptedParameter) {
            String envVariableName = converter.convertKeyToEnv(jobParam.getKey());
            envAccess.put(envVariableName, jobParam.getValue());
        } else {
            if (wellknown) {
                LOG.debug("Wellknown parameter found - but not available inside script: {}", jobParam.getKey());
            } else {
                LOG.warn("Ignored invalid job parameter key: {} for product id: {} !", jobParam.getKey(), productSetup.getId());
            }
        }
    }

    private boolean isJobParameterAcceptedByPDSServerConfiguration(PDSExecutionParameterEntry jobParam, List<PDSProductParameterDefinition> definitions) {
        for (PDSProductParameterDefinition paramDef : definitions) {
            if (paramDef.getKey().equals(jobParam.getKey())) {
                return true;
            }
        }
        return false;
    }
}
