package com.mercedesbenz.sechub.systemtest.runtime;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;
import com.mercedesbenz.sechub.systemtest.config.SecHubConfigurationDefinition;
import com.mercedesbenz.sechub.systemtest.config.SecHubExecutorConfigDefinition;

public class SystemTestRuntimeHealthCheck {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeHealthCheck.class);

    public void check(SystemTestRuntimeContext context) {
        if (!context.isLocalRun()) {
            LOG.debug("Skip local health check parts - run is not local");
        }
        SecHubConfigurationDefinition sechub = context.getLocalSecHubConfigurationOrFail();
        SystemTestRuntimeMetaData metaData = context.getRuntimeMetaData();

        verifyProductIdentifiersForPDS(sechub, metaData, context);
    }

    private void verifyProductIdentifiersForPDS(SecHubConfigurationDefinition sechubConfig, SystemTestRuntimeMetaData runtimeMetaData,
            SystemTestRuntimeContext context) {
        Set<String> allProductIdentifiers = new LinkedHashSet<>();

        /*
         * verify the server configurations are not having a duplicated product
         * identifier
         */
        Collection<PDSServerConfiguration> serverConfigurations = runtimeMetaData.getPDSServerConfigurations();
        for (PDSServerConfiguration serverConfig : serverConfigurations) {
            List<PDSProductSetup> productSetups = serverConfig.getProducts();
            for (PDSProductSetup productSetup : productSetups) {
                String foundProductId = productSetup.getId();
                if (allProductIdentifiers.contains(foundProductId)) {
                    throw new IllegalStateException("This is odd: There are two PDS product setups which do both define a product with ID:" + foundProductId
                            + "! This may never happen!\nPlease change your PDS server configuration file and provide another product ID!");
                }
                allProductIdentifiers.add(foundProductId);
            }
        }

        /*
         * verify the defined SecHub configuration parts are using the available product
         * identifiers
         */
        for (SecHubExecutorConfigDefinition executorDefinition : sechubConfig.getExecutors()) {
            String productId = executorDefinition.getPdsProductId();
            if (!allProductIdentifiers.contains(productId)) {
                throw new WrongConfigurationException("Cannot resolve PDS product.\n" + productId + " not found"
                        + "\nFound product identifiers inside PDS server configuration(s):\n" + allProductIdentifiers, context);
            }
        }
    }
}
