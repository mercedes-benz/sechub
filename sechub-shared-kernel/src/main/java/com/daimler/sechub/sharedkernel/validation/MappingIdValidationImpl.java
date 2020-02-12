package com.daimler.sechub.sharedkernel.validation;

import org.springframework.stereotype.Component;

@Component
public class MappingIdValidationImpl extends AbstractSimpleStringValidation implements MappingIdValidation {

    @Override
    protected void setup(AbstractValidation<String>.ValidationConfig config) {
        config.maxLength=80;
        config.minLength=5;
    }

    @Override
    protected void validate(ValidationContext<String> context) {
        validateNotNull(context);

        validateLength(context);
        
    }

}
