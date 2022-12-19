// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.mapping.MappingData;
import com.mercedesbenz.sechub.commons.mapping.MappingEntry;

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

        for (MappingEntry entry : data.getEntries()) {
            ValidationResult result = mappingEntryValidation.validate(entry);
            if (result.isValid()) {
                continue;
            }
            addErrorMessage(context, result.getErrorDescription());
        }

    }

    @Override
    protected String getValidatorName() {
        return "mapping data validation";
    }

}
