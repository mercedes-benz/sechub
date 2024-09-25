// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.PDSNotAcceptableException;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductParameterDefinition;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSProductIdentifierValidator;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionParameterEntry;

@Component
public class PDSJobConfigurationValidator {

    private static final Logger LOG = LoggerFactory.getLogger(PDSJobConfigurationValidator.class);

    @Autowired
    PDSProductIdentifierValidator productIdentifierValidator;

    @Autowired
    PDSServerConfigurationService serverConfigurationService;

    public void assertPDSConfigurationValid(PDSJobConfiguration configuration) {
        String message = createValidationErrorMessage(configuration);
        if (message == null) {
            return;
        }
        LOG.error("pds job configuration not valid - message:{}", message);

        throw new PDSNotAcceptableException("Configuration invalid:" + message);

    }

    private String createValidationErrorMessage(PDSJobConfiguration configuration) {
        if (configuration == null) {
            return "may not be null!";
        }
        /* check sechub uuid available */
        if (configuration.getSechubJobUUID() == null) {
            return "sechub job UUID not set!";
        }
        /* check product id valid at all */
        String productId = configuration.getProductId();
        String productIdErrorMessage = productIdentifierValidator.createValidationErrorMessage(productId);
        if (productIdErrorMessage != null) {
            return productIdErrorMessage;
        }
        /* check setup */
        PDSProductSetup productSetup = serverConfigurationService.getProductSetupOrNull(productId);
        if (productSetup == null) {
            return "configured PDS instance does not encryptionSupport product identifier:" + productId;
        }
        List<PDSProductParameterDefinition> mandatories = productSetup.getParameters().getMandatory();
        for (PDSProductParameterDefinition mandatory : mandatories) {
            String mandatoryKey = mandatory.getKey();
            if (mandatoryKey == null || mandatoryKey.isEmpty()) {
                continue;
            }
            boolean found = false;
            for (PDSExecutionParameterEntry param : configuration.getParameters()) {
                if (mandatoryKey.equals(param.getKey())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (!mandatory.hasDefault()) {
                    return "mandatory parameter not found:'" + mandatoryKey + "'";
                }
            }
        }
        return null;
    }

}
