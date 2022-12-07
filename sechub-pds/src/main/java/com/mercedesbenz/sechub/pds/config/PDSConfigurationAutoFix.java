package com.mercedesbenz.sechub.pds.config;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationType;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationTypeListParser;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;

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
        ensureSupportedDatatypesDefined(setup);
    }

    private void ensureSupportedDatatypesDefined(PDSProductSetup productSetup) {
        PDSProductParameterSetup parameters = productSetup.getParameters();
        String key = PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SUPPORTED_DATATYPES;

        PDSProductParameterDefinition supportedDataTypes = findDefinition(key, parameters.getMandatory());
        if (supportedDataTypes == null) {
            supportedDataTypes = findDefinition(key, parameters.getOptional());
        }

        if (supportedDataTypes == null) {
            supportedDataTypes = new PDSProductParameterDefinition();
            supportedDataTypes.setKey(key);
            supportedDataTypes.setDefault(FALLBACK_SUPPORTED_DATATYPES);

            parameters.getMandatory().add(supportedDataTypes);

            LOG.info("'{}' missing for product: {}, so created mandatory job parameter with default value: {}", key, productSetup.getId(),
                    supportedDataTypes.getDefault());
        }

        Set<SecHubDataConfigurationType> types = typeListParser.fetchTypesAsSetOrNull(supportedDataTypes.getDefault());
        if (types == null) {
            supportedDataTypes.setDefault(FALLBACK_SUPPORTED_DATATYPES);

            LOG.warn("'{}' for product: {} did contain invalid values - changed to {}", key, productSetup.getId(), supportedDataTypes.getDefault());
        }

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
