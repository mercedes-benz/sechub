// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;

@Service
public class PDSSummaryLogService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSSummaryLogService.class);

    @Autowired
    PDSServerConfigurationService configurationService;

    @EventListener(ApplicationReadyEvent.class)
    void applicationReady() {
        PDSServerConfiguration configuration = configurationService.getServerConfiguration();

        StringBuilder summary = new StringBuilder();
        summary.append("PDS has been started successfully.\n**************************\n        Summary\n**************************");
        summary.append("\n- config file used: ").append(configurationService.pathToConfigFile);

        summary.append("\n- server id: ").append(configuration.getServerId());
        summary.append("\n- system wide minutes to wait for product: ").append(configurationService.getMinutesToWaitForProduct());
        summary.append("\n- minimum configurable minutes to wait for product: ").append(configurationService.getMinimumConfigurableMinutesToWaitForProduct());
        summary.append("\n- maximum configurable minutes to wait for product: ").append(configurationService.getMaximumConfigurableMinutesToWaitForProduct());

        List<PDSProductSetup> products = configuration.getProducts();
        summary.append("\n- Available products: ").append(products.size());
        for (PDSProductSetup setup : products) {
            String productId = setup.getId();
            String defaultSuppportedDataTypes = configurationService.getProductParameterDefaultValueOrNull(productId,
                    PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES);
            summary.append("\n  * ").append(productId);
            if (defaultSuppportedDataTypes != null) {
                summary.append("- ");
                summary.append(defaultSuppportedDataTypes);
            }
            PDSProductParameterSetup parameterSetup = setup.getParameters();
            appendParameterInfo(summary, "mandatory", parameterSetup.getMandatory());
            appendParameterInfo(summary, "optional", parameterSetup.getOptional());
            appendParameterInfo(summary, "optional", parameterSetup.getOptional());
            summary.append("\n    - envWhitelist: ").append(setup.getEnvWhitelist());
        }

        LOG.info(summary.toString());

    }

    private void appendParameterInfo(StringBuilder summary, String type, List<PDSProductParameterDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return;
        }
        /* sort parameters by key */
        PDSProductParameterDefinition[] sortedDefinitionArray = definitions.toArray(new PDSProductParameterDefinition[definitions.size()]);
        Arrays.sort(definitions.toArray(sortedDefinitionArray), new PDSProductParameterDefinitionKeyComparator());

        summary.append("\n    - ").append(type);
        for (PDSProductParameterDefinition parameterDefinition : sortedDefinitionArray) {
            summary.append("\n       ").append(parameterDefinition.getKey());
            String defaultValue = parameterDefinition.getDefault();
            if (defaultValue != null && !defaultValue.isBlank()) {
                summary.append(" (default: ").append(defaultValue).append(")");
            }

        }
    }

    private class PDSProductParameterDefinitionKeyComparator implements Comparator<PDSProductParameterDefinition> {

        @Override
        public int compare(PDSProductParameterDefinition o1, PDSProductParameterDefinition o2) {
            if (o1 == null) {
                if (o2 == null) {
                    return 0;
                }
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            String key1 = o1.getKey();
            if (key1 == null) {
                return -1;
            }
            String key2 = o2.getKey();
            if (key2 == null) {
                return 1;
            }
            return key1.compareTo(key2);
        }

    }
}
