// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.execution;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.config.PDSProductSetup;
import com.daimler.sechub.pds.config.PDSProdutParameterDefinition;
import com.daimler.sechub.pds.config.PDSProdutParameterSetup;
import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.job.PDSJobConfiguration;

@Service
public class PDSExecutionEnvironmentService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionEnvironmentService.class);

    @Autowired
    PDSKeyToEnvConverter converter;

    @Autowired
    PDSServerConfigurationService serverConfigService;

    public Map<String, String> buildEnvironmentMap(PDSJobConfiguration config) {

        String productId = config.getProductId();
        PDSProductSetup productSetup = serverConfigService.getProductSetupOrNull(productId);
        if (productSetup == null) {
            LOG.error("No product setup found for product id:{}", productId);
            return Collections.emptyMap();
        }

        Map<String, String> map = new LinkedHashMap<>();

        List<PDSExecutionParameterEntry> jobParams = config.getParameters();
        for (PDSExecutionParameterEntry jobParam : jobParams) {
            addJobParamDataWhenAccepted(productSetup, jobParam, map);
        }
        return map;
    }

    private void addJobParamDataWhenAccepted(PDSProductSetup productSetup, PDSExecutionParameterEntry jobParam, Map<String, String> map) {
        PDSProdutParameterSetup params = productSetup.getParameters();
        if (isJobParameterValid(jobParam, params.getMandatory()) || isJobParameterValid(jobParam, params.getOptional())) {
            map.put(converter.convertKeyToEnv(jobParam.getKey()), jobParam.getValue());
        } else {
            LOG.warn("Ignored invalid job parameter key {} for product id:{} !", jobParam.getKey(), productSetup.getId());
        }
    }

    private boolean isJobParameterValid(PDSExecutionParameterEntry jobParam, List<PDSProdutParameterDefinition> definitions) {
        for (PDSProdutParameterDefinition paramDef : definitions) {
            if (paramDef.getKey().equals(jobParam.getKey())) {
                return true;
            }
        }
        return false;
    }
}
