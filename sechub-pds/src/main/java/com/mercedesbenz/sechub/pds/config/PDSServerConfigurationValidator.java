// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;

@Component
public class PDSServerConfigurationValidator {

    @Autowired
    PDSProductIdentifierValidator productIdValidator;

    @Autowired
    PDSServerIdentifierValidator serverIdValidator;

    @Autowired
    PDSPathExecutableValidator pathExecutableValidator;

    public String createValidationErrorMessage(PDSServerConfiguration configuration) {
        if (configuration == null) {
            return "configuration may not be null!";
        }
        String serverId = configuration.getServerId();
        String serverIdProblem = serverIdValidator.createValidationErrorMessage(serverId);
        if (serverIdProblem != null) {
            return "server id problem:" + serverIdProblem;
        }

        List<PDSProductSetup> products = configuration.getProducts();
        if (products.size() == 0) {
            return "at least one product setup must be defined, but there is none!";
        }
        for (PDSProductSetup setup : products) {
            String productId = setup.getId();
            String productIdErrorMessage = productIdValidator.createValidationErrorMessage(productId);
            if (productIdErrorMessage != null) {
                return productIdErrorMessage;
            }
            String path = setup.getPath();
            String pathProblem = pathExecutableValidator.createValidationErrorMessage(path);
            if (pathProblem != null) {
                return "setup of product:" + productId + " invalid, because path problem:" + pathProblem;
            }
        }
        return null;
    }
}
