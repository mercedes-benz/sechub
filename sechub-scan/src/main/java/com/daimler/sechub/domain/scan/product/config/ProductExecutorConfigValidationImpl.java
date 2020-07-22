// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.util.JSONConverterException;
import com.daimler.sechub.sharedkernel.validation.AbstractValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationContext;

@Component
public class ProductExecutorConfigValidationImpl extends AbstractValidation<ProductExecutorConfig> implements ProductExecutorConfigValidation {

    private static final Logger LOG = LoggerFactory.getLogger(ProductExecutorConfigValidationImpl.class);

    protected String getValidatorName() {
        return "product executor config validation";
    }

    @Override
    protected void setup(AbstractValidation<ProductExecutorConfig>.ValidationConfig config) {

    }

    @Override
    protected void validate(ValidationContext<ProductExecutorConfig> context) {
        validateNotNull(context);
        ProductExecutorConfig config = getObjectToValidate(context);
        String setup = config.getSetup();
        /* just check if can be transformed to json */
        try {
            ProductExecutorConfigSetup.fromJSONString(setup);
        } catch (JSONConverterException e) {
            LOG.error("setup validation failed - because of JSON conversion failure", e);
            addErrorMessage(context, "setup cannot be deserialized!");
        }

    }

}
