// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ProjectMetaDataValidationImpl extends AbstractValidation<Map<String, String>> implements ProjectMetaDataValidation {
    
    public static final int METADATA_KEY_LENGTH_MAX = 60;
    public static final int METADATA_VALUE_LENGTH_MAX = 255;

    @Override
    protected String getValidatorName() {
        return "MetaData validation";
    }

    @Override
    protected void setup(AbstractValidation<Map<String, String>>.ValidationConfig config) {
    }

    @Override
    protected void validate(ValidationContext<Map<String, String>> context) {
        
        validateNotNull(context);
        Map<String, String> metaData = context.objectToValidate;
        if (metaData == null) {
            /* already handled before */
            return;
        }
        
        metaData.entrySet().forEach(entry -> {
            
            if (entry.getKey() == null || entry.getValue() == null) {
                addErrorMessage(context, "MetaData key nor value should be null");
                return;
            }
            
            if (entry.getKey().length() > METADATA_KEY_LENGTH_MAX) {
                addErrorMessage(context, "MetaData key " + entry.getKey() + " exceeds maximum of " + METADATA_KEY_LENGTH_MAX + " characters");
                return;
            }
            
            if (entry.getValue().length() > METADATA_VALUE_LENGTH_MAX) {
                addErrorMessage(context, "MetaData value " + entry.getValue() + " exceeds maximum of " + METADATA_VALUE_LENGTH_MAX + " characters");
                return;
            }
        });        
    }
}
