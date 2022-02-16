// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.validation.AbstractValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationContext;

@Component
public class FalsePositiveJobDataListValidationImpl extends AbstractValidation<FalsePositiveJobDataList> implements FalsePositiveJobDataListValidation {

    @Autowired
    ApiVersionValidationFactory apiVersionValidationFactory;

    private ApiVersionValidation apiVersionValidation;

    @Autowired
    FalsePositiveJobDataValidationImpl falsePositiveJobDataValidation;

    @PostConstruct
    void postConstruct() {
        apiVersionValidation = apiVersionValidationFactory.createValidationAccepting("1.0");
    }

    @Override
    protected void setup(AbstractValidation<FalsePositiveJobDataList>.ValidationConfig config) {
        config.minLength = 1;
        config.maxLength = 500; // we allow maximum 500 entries in one list
    }

    @Override
    protected String getValidatorName() {
        return "false positive list validation";
    }

    @Override
    protected void validate(ValidationContext<FalsePositiveJobDataList> context) {
        validateNotNull(context);
        FalsePositiveJobDataList target = getObjectToValidate(context);

        validateVersion(context, target);
        if (context.isInValid()) {
            return;
        }

        validateType(context, target);
        if (context.isInValid()) {
            return;
        }

        validateJobData(context, target);

    }

    private void validateJobData(ValidationContext<FalsePositiveJobDataList> context, FalsePositiveJobDataList target) {
        List<FalsePositiveJobData> jobDataList = target.getJobData();
        validateNotNull(context, jobDataList, "jobDataList");

        validateMinSize(context, jobDataList, 1, "jobDataList");
        validateMaxSize(context, jobDataList, getConfig().maxLength, "jobDataList");

        if (context.isInValid()) {
            return;
        }

        for (FalsePositiveJobData jobData : jobDataList) {
            falsePositiveJobDataValidation.validate(jobData);
        }
    }

    private void validateType(ValidationContext<FalsePositiveJobDataList> context, FalsePositiveJobDataList target) {
        validateContainsExpectedOnly(context, "given type not known", target.getType(), FalsePositiveJobDataList.ACCEPTED_TYPE);
    }

    private void validateVersion(ValidationContext<FalsePositiveJobDataList> context, FalsePositiveJobDataList target) {
        context.addErrors(apiVersionValidation.validate(target.getApiVersion()));
    }

}
