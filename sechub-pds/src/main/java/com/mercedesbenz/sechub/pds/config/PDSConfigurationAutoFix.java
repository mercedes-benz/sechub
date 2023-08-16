// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants.*;
import static com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterValueConstants.*;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationTypeListParser;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;

@Component
public class PDSConfigurationAutoFix {
    private static final Logger LOG = LoggerFactory.getLogger(PDSConfigurationAutoFix.class);

    private static final String FALLBACK_SUPPORTED_DATATYPES = SecHubDataConfigurationType.SOURCE.name() + "," + SecHubDataConfigurationType.BINARY.name() + ","
            + SecHubDataConfigurationType.NONE.name();

    @Autowired
    SecHubDataConfigurationTypeListParser typeListParser;

    public void autofixWhenNecessary(PDSServerConfiguration configuration) {
        List<PDSProductSetup> products = configuration.getProducts();
        for (PDSProductSetup setup : products) {
            autoFixWhenNecessary(setup);
        }
    }

    private void autoFixWhenNecessary(PDSProductSetup setup) {
        ensureReadResilienceSetup(setup);
        ensureSupportedDatatypesDefined(setup);
    }

    private void ensureReadResilienceSetup(PDSProductSetup productSetup) {
        ensureProductParameterDefinition(productSetup, PARAM_KEY_PDS_CONFIG_JOBSTORAGE_READ_RESILIENCE_RETRIES_MAX,
                DEFAULT_JOBSTORAGE_READ_RESILIENCE_RETRIES_MAX);
        ensureProductParameterDefinition(productSetup, PARAM_KEY_PDS_CONFIG_JOBSTORAGE_READ_RESILIENCE_RETRY_WAIT_SECONDS,
                DEFAULT_JOBSTORAGE_READ_RESILIENCE_RETRY_WAIT_SECONDS);
    }

    private void ensureSupportedDatatypesDefined(PDSProductSetup productSetup) {
        String key = PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES;
        String fallbackDefaultValue = FALLBACK_SUPPORTED_DATATYPES;

        // create if missing
        PDSProductParameterDefinition supportedDataTypes = ensureProductParameterDefinition(productSetup, key, fallbackDefaultValue);

        // check if defined types are valid - if not, fix it...
        Set<SecHubDataConfigurationType> types = typeListParser.fetchTypesAsSetOrNull(supportedDataTypes.getDefault());
        if (types == null) {
            supportedDataTypes.setDefault(fallbackDefaultValue);

            LOG.warn("'{}' for product: {} did contain invalid values - changed to {}", key, productSetup.getId(), supportedDataTypes.getDefault());
        }

    }

    private PDSProductParameterDefinition ensureProductParameterDefinition(PDSProductSetup productSetup, String key, int defaultValue) {
        return ensureProductParameterDefinition(productSetup, key, String.valueOf(defaultValue));

    }

    private PDSProductParameterDefinition ensureProductParameterDefinition(PDSProductSetup productSetup, String key, String defaultValue) {
        PDSProductParameterSetup parameters = productSetup.getParameters();
        PDSProductParameterDefinition supportedDataTypes = findDefinition(key, parameters.getMandatory());
        if (supportedDataTypes == null) {
            supportedDataTypes = findDefinition(key, parameters.getOptional());
        }

        if (supportedDataTypes == null) {
            supportedDataTypes = new PDSProductParameterDefinition();
            supportedDataTypes.setKey(key);
            supportedDataTypes.setDefault(defaultValue);

            parameters.getMandatory().add(supportedDataTypes);

            LOG.info("'{}' missing for product: {}, so created mandatory job parameter with default value: {}", key, productSetup.getId(),
                    supportedDataTypes.getDefault());
        }
        return supportedDataTypes;
    }

    private PDSProductParameterDefinition findDefinition(String searchedKey, List<PDSProductParameterDefinition> definitions) {
        for (PDSProductParameterDefinition definition : definitions) {
            String key = definition.getKey();
            if (searchedKey.equals(key)) {
                return definition;
            }
        }
        return null;
    }
}
