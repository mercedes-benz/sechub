package com.daimler.sechub.sharedkernel.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.mapping.MappingData;
import com.daimler.sechub.sharedkernel.mapping.MappingEntry;

@Component
public class MappingDataValidationImpl extends AbstractValidation<MappingData> implements MappingDataValidation {

    @Autowired
    MappingEntryValidation mappingEntryValidation;
    
    @Override
    protected void setup(AbstractValidation<MappingData>.ValidationConfig config) {
    }

    @Override
    protected void validate(ValidationContext<MappingData> context) {
        validateNotNull(context);
        
        MappingData data = context.objectToValidate;
        
        for (MappingEntry entry: data.getEntries()) {
            ValidationResult result = mappingEntryValidation.validate(entry);
            if (result.isValid()) {
                continue;
            }
            context.addError(result.getErrorDescription());
        }
        
    }
   
}
