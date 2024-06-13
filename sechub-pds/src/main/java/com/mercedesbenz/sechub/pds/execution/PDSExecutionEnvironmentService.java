// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static com.mercedesbenz.sechub.commons.pds.PDSLauncherScriptEnvironmentConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
            throw new IllegalArgumentException("pds job config may not be null!");
        }
        if (builder == null) {
            throw new IllegalArgumentException("pds job config may not be null!");
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
        Map<String, String> environment = initCleanEnvironment(productSetup, builder);

        addPdsJobRelatedVariables(pdsJobUUID, environment);
        addPdsExecutorJobParameters(productSetup, config, environment);
        addSecHubJobUUIDAsEnvironmentEntry(config, environment);

        replaceNullValuesWithEmptyStrings(environment);

        LOG.debug("Initialized environment variables for script of pds job: {} with: {}", pdsJobUUID, environment);
    }

    private Map<String, String> initCleanEnvironment(PDSProductSetup productSetup, ProcessBuilder builder) {
        Map<String, String> environment = builder.environment();

        Set<String> pdsScriptEnvWhitelist = productSetup.getEnvWhitelist();
        LOG.debug("PDS script environment variable white list: '{}'", pdsScriptEnvWhitelist);

        environmentCleaner.clean(environment, pdsScriptEnvWhitelist);

        return environment;

    }

    private void addPdsJobRelatedVariables(UUID pdsJobUUID, Map<String, String> map) {
        WorkspaceLocationData locationData = workspaceService.createLocationData(pdsJobUUID);

        map.put(PDS_JOB_WORKSPACE_LOCATION, locationData.getWorkspaceLocation());
        map.put(PDS_JOB_RESULT_FILE, locationData.getResultFileLocation());
        map.put(PDS_JOB_USER_MESSAGES_FOLDER, locationData.getUserMessagesLocation());
        map.put(PDS_JOB_EVENTS_FOLDER, locationData.getEventsLocation());
        map.put(PDS_JOB_METADATA_FILE, locationData.getMetaDataFileLocation());
        map.put(PDS_JOB_UUID, pdsJobUUID.toString());
        map.put(PDS_JOB_SOURCECODE_ZIP_FILE, locationData.getSourceCodeZipFileLocation());
        map.put(PDS_JOB_BINARIES_TAR_FILE, locationData.getBinariesTarFileLocation());

        String extractedSourcesLocation = locationData.getExtractedSourcesLocation();

        map.put(PDS_JOB_SOURCECODE_UNZIPPED_FOLDER, extractedSourcesLocation);
        map.put(PDS_JOB_EXTRACTED_SOURCES_FOLDER, extractedSourcesLocation);

        String extractedBinariesLocation = locationData.getExtractedBinariesLocation();
        map.put(PDS_JOB_EXTRACTED_BINARIES_FOLDER, extractedBinariesLocation);

        map.put(PDS_JOB_HAS_EXTRACTED_SOURCES, "" + workspaceService.hasExtractedSources(pdsJobUUID));
        map.put(PDS_JOB_HAS_EXTRACTED_BINARIES, "" + workspaceService.hasExtractedBinaries(pdsJobUUID));

    }

    private void addPdsExecutorJobParameters(PDSProductSetup productSetup, PDSJobConfiguration config, Map<String, String> map) {

        List<PDSExecutionParameterEntry> jobParams = config.getParameters();
        for (PDSExecutionParameterEntry jobParam : jobParams) {
            addJobParamDataWhenAccepted(productSetup, jobParam, map);
        }

        addDefaultsForMissingParameters(productSetup, map);

    }

    /*
     * Replace null values with empty strings to avoid problems with environment map
     * of process builder: This map does throw an exception in this case (index of
     * problems)
     */
    private void replaceNullValuesWithEmptyStrings(Map<String, String> map) {

        List<String> keysForEntriesWithNullValue = new ArrayList<>();

        for (Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                keysForEntriesWithNullValue.add(entry.getKey());
            }
        }

        for (String keyForEntryWithNullValue : keysForEntriesWithNullValue) {
            map.put(keyForEntryWithNullValue, "");

            LOG.warn("Replaced null value for key: {} with empty string", keyForEntryWithNullValue);
        }

    }

    private void addDefaultsForMissingParameters(PDSProductSetup productSetup, Map<String, String> map) {
        PDSProductParameterSetup parameters = productSetup.getParameters();

        addDefaultsForMissingParametersInList(parameters.getMandatory(), map);
        addDefaultsForMissingParametersInList(parameters.getOptional(), map);
    }

    private void addDefaultsForMissingParametersInList(List<PDSProductParameterDefinition> parameterDefinitions, Map<String, String> map) {

        for (PDSProductParameterDefinition parameterDefinition : parameterDefinitions) {
            if (!parameterDefinition.hasDefault()) {
                continue;
            }
            String envVariableName = converter.convertKeyToEnv(parameterDefinition.getKey());

            String value = map.get(envVariableName);

            if (value == null) {
                map.put(envVariableName, parameterDefinition.getDefault());
            }
        }

    }

    private void addSecHubJobUUIDAsEnvironmentEntry(PDSJobConfiguration config, Map<String, String> map) {
        map.put(PDSLauncherScriptEnvironmentConstants.SECHUB_JOB_UUID, fetchSecHubJobUUIDasString(config));
    }

    private String fetchSecHubJobUUIDasString(PDSJobConfiguration pdsJobConfiguration) {
        UUID sechubJobUUID = pdsJobConfiguration.getSechubJobUUID();
        if (sechubJobUUID == null) {
            LOG.error("No SecHub job UUID found, environment variable: {} will be empty", PDSLauncherScriptEnvironmentConstants.SECHUB_JOB_UUID);
            return "";
        }
        return sechubJobUUID.toString();
    }

    private void addJobParamDataWhenAccepted(PDSProductSetup productSetup, PDSExecutionParameterEntry jobParam, Map<String, String> map) {
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
            map.put(envVariableName, jobParam.getValue());
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
