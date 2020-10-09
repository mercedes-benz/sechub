// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.JSONConverterException;
import com.daimler.sechub.sharedkernel.validation.AbstractValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationContext;
import com.daimler.sechub.sharedkernel.validation.ValidationResult;

@Component
public class ProductExecutorConfigValidationImpl extends AbstractValidation<ProductExecutorConfig> implements ProductExecutorConfigValidation {

    private static final Logger LOG = LoggerFactory.getLogger(ProductExecutorConfigValidationImpl.class);
    
    @Autowired
    ProductExecutorConfigSetupValidation setupValidation;
    
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
        String name = config.getName();
        validateNotNull(context,name, "name");
        validateMaxLength(context,name, 30, "name");
        validateMinLength(context,name, 3, "name");
        
        validateNotNull(context,config.getProductIdentifier(),"productIdentifier");
        validateNotNull(context,config.getExecutorVersion(),"executorVersion");
        
        ProductExecutorConfigSetup setup = config.getSetup();
        validateNotNull(context, setup,"setup");
        if (setup==null) {
            LOG.error("setup null - not valid");
            return;
        }
        /* just check if can be transformed to json */
        try {
            ValidationResult setupResult = setupValidation.validate(setup);
            context.addErrors(setupResult);
            
        } catch (JSONConverterException e) {
            LOG.error("setup validation failed - because of JSON conversion failure", e);
            addErrorMessage(context, "setup cannot be deserialized!");
        }

    }

}
