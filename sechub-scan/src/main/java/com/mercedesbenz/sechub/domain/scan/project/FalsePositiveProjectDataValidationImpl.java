// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.validation.AbstractValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationContext;

@Component
public class FalsePositiveProjectDataValidationImpl extends AbstractValidation<FalsePositiveProjectData> implements FalsePositiveProjectDataValidation {

    @Autowired
    FalsePositiveProjectDataIdValidation idValidation;

    @Autowired
    WebscanFalsePositiveProjectDataValidation webscanValidation;

    @Override
    protected void setup(AbstractValidation<FalsePositiveProjectData>.ValidationConfig config) {
        config.maxLength = 500; // we allow maximum 500 chars for comments
    }

    @Override
    protected void validate(ValidationContext<FalsePositiveProjectData> context) {
        validateNotNull(context);

        if (context.isInValid()) {
            return;
        }

        FalsePositiveProjectData target = getObjectToValidate(context);

        String commentName = FalsePositiveDataList.PROPERTY_PROJECTDATA + "." + FalsePositiveProjectData.PROPERTY_COMMENT;

        validateMaxLength(context, target.getComment(), getConfig().maxLength, commentName);

        context.addErrors(idValidation.validate(target.getId()));
        context.addErrors(webscanValidation.validate(target.getWebScan()));
    }

    @Override
    protected String getValidatorName() {
        return "false positive project data validation";
    }

}
