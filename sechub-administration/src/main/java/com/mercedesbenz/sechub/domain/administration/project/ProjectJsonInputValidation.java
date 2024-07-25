// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration.project;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ApiVersionValidationFactory;
import com.mercedesbenz.sechub.sharedkernel.validation.ProjectIdValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ProjectMetaDataValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.URIValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.UserIdValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.ValidationResult;

import jakarta.annotation.PostConstruct;

@Component
public class ProjectJsonInputValidation {

    @Autowired
    ApiVersionValidationFactory apiVersionValidationFactory;

    @Autowired
    ProjectIdValidation projectIdValidation;;

    @Autowired
    UserIdValidation userIdValidation;

    @Autowired
    URIValidation whitelistValidation;

    @Autowired
    ProjectMetaDataValidation metaDataValidation;

    private ApiVersionValidation apiVersionValidation;

    @PostConstruct
    void postConstruct() {
        apiVersionValidation = apiVersionValidationFactory.createValidationAccepting("1.0");
    }

    public boolean supports(Class<?> clazz) {
        return ProjectJsonInput.class.isAssignableFrom(clazz);
    }

    public ProjectJsonInput asInput(Object target) {
        return (ProjectJsonInput) target;
    }

    public void checkProjectId(Errors errors, ProjectJsonInput input) {
        ValidationResult projectIdValidationResult = projectIdValidation.validate(input.getName());
        if (!projectIdValidationResult.isValid()) {
            errors.rejectValue(ProjectJsonInput.PROPERTY_NAME, "api.error.projectid.invalid", projectIdValidationResult.getErrorDescription());
        }
    }

    public void checkApiVersion(Errors errors, ProjectJsonInput input) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, ProjectJsonInput.PROPERTY_API_VERSION, "field.required");

        String apiVersion = input.getApiVersion();
        if (apiVersion == null) {
            return; /* handled before */
        }
        ValidationResult apiValidationResult = apiVersionValidation.validate(apiVersion);
        if (!apiValidationResult.isValid()) {
            errors.rejectValue(ProjectJsonInput.PROPERTY_API_VERSION, "api.error.unsupported.version", apiValidationResult.getErrorDescription());
        }
    }

    public void checkOwnerUserId(Errors errors, ProjectJsonInput input) {
        ValidationResult userIdValidationResult = userIdValidation.validate(input.getOwner());
        if (!userIdValidationResult.isValid()) {
            errors.rejectValue(ProjectJsonInput.PROPERTY_OWNER, "api.error.userid.invalid", userIdValidationResult.getErrorDescription());

        }
    }

    public void checkWhitelist(Errors errors, ProjectJsonInput input) {

        if (!input.getWhiteList().isPresent()) {
            return;
        }

        input.getWhiteList().get().getUris().forEach(uri -> {
            ValidationResult uriValidationResult = whitelistValidation.validate(uri);
            if (!uriValidationResult.isValid()) {
                errors.rejectValue(ProjectJsonInput.PROPERTY_WHITELIST, "api.error.whitelistentry.invalid", uriValidationResult.getErrorDescription());
            }
        });
    }

    public void checkMetaData(Errors errors, ProjectJsonInput input) {

        if (!input.getMetaData().isPresent()) {
            return;
        }

        Map<String, String> metaDataMap = input.getMetaData().get().getMetaDataMap();

        ValidationResult metaDataValidationResult = metaDataValidation.validate(metaDataMap);
        if (!metaDataValidationResult.isValid()) {
            errors.rejectValue(ProjectJsonInput.PROPERTY_METADATA, "api.error.metadataentry.invalid", metaDataValidationResult.getErrorDescription());
        }
    }
}
