// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.validation.AbstractValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationContext;

@Component
public class FalsePositiveProjectDataValidationImpl extends AbstractValidation<FalsePositiveProjectData> implements FalsePositiveProjectDataValidation {

    private static final String VALIDATOR_NAME = "false positive project data validation";

    private static final int MAXIMUM_CHARS_FOR_COMMENTS = 500;

    private final FalsePositiveProjectDataIdValidation idValidation;
    private final WebscanFalsePositiveProjectDataValidation webscanValidation;

    public FalsePositiveProjectDataValidationImpl(FalsePositiveProjectDataIdValidation idValidation,
            WebscanFalsePositiveProjectDataValidation webscanValidation) {
        this.idValidation = idValidation;
        this.webscanValidation = webscanValidation;

    }

    @Override
    protected void setup(AbstractValidation<FalsePositiveProjectData>.ValidationConfig config) {
        config.maxLength = MAXIMUM_CHARS_FOR_COMMENTS;
    }

    @Override
    protected void validate(ValidationContext<FalsePositiveProjectData> context) {
        validateNotNull(context);

        if (context.isInValid()) {
            return;
        }

        FalsePositiveProjectData target = getObjectToValidate(context);

        String commentName = "%s.%s".formatted(FalsePositiveDataList.PROPERTY_PROJECTDATA, FalsePositiveProjectData.PROPERTY_COMMENT);

        validateMaxLength(context, target.getComment(), getConfig().maxLength, commentName);

        context.addErrors(idValidation.validate(target.getId()));
        context.addErrors(webscanValidation.validate(target.getWebScan()));
    }

    @Override
    protected String getValidatorName() {
        return VALIDATOR_NAME;
    }

}
