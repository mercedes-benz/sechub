// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.pds.ExecutionPDSKey;
import com.mercedesbenz.sechub.commons.pds.PDSConfigDataKeyProvider;
import com.mercedesbenz.sechub.commons.pds.PDSLauncherScriptEnvironmentConstants;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.job.PDSJobConfiguration;

@Service
public class PDSExecutionEnvironmentService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionEnvironmentService.class);

    @Autowired
    PDSKeyToEnvConverter converter;

    @Autowired
    PDSServerConfigurationService serverConfigService;

    @Autowired
    PDSExecutionEnvironmentPrepare pdsExecutionEnvironmentPrepare;

    public Map<String, String> buildEnvironmentMap(PDSJobConfiguration config) {
        Map<String, String> map = new LinkedHashMap<>();

        String productId = config.getProductId();
        PDSProductSetup productSetup = serverConfigService.getProductSetupOrNull(productId);
        if (productSetup != null) {
            List<PDSExecutionParameterEntry> jobParams = config.getParameters();
            for (PDSExecutionParameterEntry jobParam : jobParams) {
                addJobParamDataWhenAccepted(productSetup, jobParam, map);
            }

            addDefaultsForMissingParameters(productSetup, map);

            if (ScanType.PREPARE.equals(productSetup.getScanType())) {
                map.putAll(pdsExecutionEnvironmentPrepare.getPDSStorageProperties());
            }

        } else {
            LOG.error("No product setup found for product id:{}", productId);
        }
        addSecHubJobUUIDAsEnvironmentEntry(config, map);

        replaceNullValuesWithEmptyStrings(map);

        return map;
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

    private String fetchSecHubJobUUIDasString(PDSJobConfiguration config) {
        UUID sechubJobUUID = config.getSechubJobUUID();
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
