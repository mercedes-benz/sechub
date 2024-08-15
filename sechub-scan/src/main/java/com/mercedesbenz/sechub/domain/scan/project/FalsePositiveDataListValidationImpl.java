// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.List;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.validation.AbstractValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationContext;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

@Component
public class FalsePositiveDataListValidationImpl extends AbstractValidation<FalsePositiveDataList> implements FalsePositiveDataListValidation {

    private static final String API_VERSION_1_0 = "1.0";
    private static final int MINIMUM_FP_DATA_LIST_SIZE = 0;
    private static final int MAXIMUM_FP_DATA_LIST_SIZE = 500;

    private static final String VALIDATOR_NAME = "false positive list validation";

    private final ApiVersionValidationFactory apiVersionValidationFactory;

    private ApiVersionValidation apiVersionValidation;

    private final FalsePositiveJobDataValidation falsePositiveJobDataValidation;

    private final FalsePositiveProjectDataValidation falsePositiveProjectDataValidation;

    /* @formatter:off */
    public FalsePositiveDataListValidationImpl(ApiVersionValidationFactory apiVersionValidationFactory,
            FalsePositiveJobDataValidation falsePositiveJobDataValidation,
            FalsePositiveProjectDataValidation falsePositiveProjectDataValidation) {

        this.apiVersionValidationFactory = apiVersionValidationFactory;
        this.falsePositiveJobDataValidation = falsePositiveJobDataValidation;
        this.falsePositiveProjectDataValidation = falsePositiveProjectDataValidation;
        /* @formatter:on */

        apiVersionValidation = this.apiVersionValidationFactory.createValidationAccepting(API_VERSION_1_0);
    }

    @Override
    protected void setup(AbstractValidation<FalsePositiveDataList>.ValidationConfig config) {
        config.minLength = MINIMUM_FP_DATA_LIST_SIZE;
        config.maxLength = MAXIMUM_FP_DATA_LIST_SIZE;
    }

    @Override
    protected String getValidatorName() {
        return VALIDATOR_NAME;
    }

    @Override
    protected void validate(ValidationContext<FalsePositiveDataList> context) {
        validateNotNull(context);
        if (context.isInValid()) {
            return;
        }

        FalsePositiveDataList target = getObjectToValidate(context);

        validateVersion(context, target);
        if (context.isInValid()) {
            return;
        }

        validateType(context, target);
        if (context.isInValid()) {
            return;
        }

        validateJobDataAndProjectDataSize(context, target);
        if (context.isInValid()) {
            return;
        }

        validateJobData(context, target.getJobData());

        validateProjectData(context, target.getProjectData());

    }

    private void validateProjectData(ValidationContext<FalsePositiveDataList> context, List<FalsePositiveProjectData> projectDataList) {
        for (FalsePositiveProjectData projectData : projectDataList) {
            ValidationResult resultForJobData = falsePositiveProjectDataValidation.validate(projectData);
            if (!resultForJobData.isValid()) {
                context.addErrors(resultForJobData);
            }
        }
    }

    private void validateJobData(ValidationContext<FalsePositiveDataList> context, List<FalsePositiveJobData> jobDataList) {
        for (FalsePositiveJobData jobData : jobDataList) {
            ValidationResult resultForJobData = falsePositiveJobDataValidation.validate(jobData);
            if (!resultForJobData.isValid()) {
                context.addErrors(resultForJobData);
            }
        }
    }

    private void validateJobDataAndProjectDataSize(ValidationContext<FalsePositiveDataList> context, FalsePositiveDataList target) {
        List<FalsePositiveJobData> jobDataList = target.getJobData();
        List<FalsePositiveProjectData> projectDataList = target.getProjectData();

        validateNotNull(context, jobDataList, "projectDataList");
        validateNotNull(context, projectDataList, "projectDataList");
        if (context.isInValid()) {
            return;
        }

        validateMinSize(context, jobDataList, getConfig().minLength, "jobDataList");
        validateMinSize(context, jobDataList, getConfig().minLength, "projectDataList");

        int combinedSize = jobDataList.size() + projectDataList.size();

        if (combinedSize > getConfig().maxLength) {
            context.addError(getValidatorName(), ": The number of specified false positives in jobDataList and projectDataList must not be greater than: %s"
                    .formatted(getConfig().maxLength));
        }
    }

    private void validateType(ValidationContext<FalsePositiveDataList> context, FalsePositiveDataList target) {
        validateContainsExpectedOnly(context, "given type not known", target.getType(), FalsePositiveDataList.ACCEPTED_TYPE,
                FalsePositiveDataList.DEPRECATED_ACCEPTED_TYPE);
    }

    private void validateVersion(ValidationContext<FalsePositiveDataList> context, FalsePositiveDataList target) {
        context.addErrors(apiVersionValidation.validate(target.getApiVersion()));
    }

}
