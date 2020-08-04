package com.daimler.sechub.domain.scan.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.validation.AbstractValidation;
import com.daimler.sechub.sharedkernel.validation.JobUUIDValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationContext;

@Component
public class FalsePositiveJobDataValidationImpl extends AbstractValidation<FalsePositiveJobData> implements FalsePositiveJobDataValidation {

    @Autowired
    JobUUIDValidation jobUUIDValidation;


    @Override
    protected void setup(AbstractValidation<FalsePositiveJobData>.ValidationConfig config) {
        config.maxLength = 500; // we allow maximum 500 chars for comments
    }

    @Override
    protected void validate(ValidationContext<FalsePositiveJobData> context) {
        validateNotNull(context);
        
        FalsePositiveJobData target = getObjectToValidate(context);
        context.addErrors(jobUUIDValidation.validate(target.getJobUUID()));
        validateMaxLength(context, target.getComment(), getConfig().maxLength, "jobData.comment");

    }
    
    @Override
    protected String getValidatorName() {
        return "false positive job data validation";
    }

}
