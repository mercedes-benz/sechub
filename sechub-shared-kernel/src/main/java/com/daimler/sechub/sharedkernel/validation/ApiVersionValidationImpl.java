// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ApiVersionValidationImpl extends AbstractSimpleStringValidation implements ApiVersionValidation {

    public static final int API_MIN_LENGTH = 3; /* 1.0 */
    public static final int API_MAX_LENGTH = 8; /* 12345.78 */

    private List<String> acceptedVersions;

    public ApiVersionValidationImpl() {
        this(new String[] { "1.0" });
    }

    ApiVersionValidationImpl(String[] acceptedAre) {
        this.acceptedVersions = new ArrayList<>();
        for (String acceptedIs : acceptedAre) {
            if (acceptedIs == null) {
                continue;
            }
            acceptedVersions.add(acceptedIs);
        }
    }

    @Override
    protected void setup(ValidationConfig config) {
        config.minLength = API_MIN_LENGTH;
        config.maxLength = API_MAX_LENGTH;
    }

    @Override
    protected void validate(ValidationContext<String> context) {
        validateNotNull(context);
        if (context.isInValid()) {
            return;
        }
        validateLength(context);
        if (context.isInValid()) {
            return;
        }
        validateSupportedVersion(context);
    }

    void validateSupportedVersion(ValidationContext<String> context) {
        String string = context.objectToValidate;
        if (string == null) {
            addErrorMessage(context, "There is no API version defined (null) so not valid, supported is:" + createSupportedApis());
            return;
        }
        for (String supported : acceptedVersions) {
            if (string.equals(supported)) {
                return;
            }
        }
        addErrorMessage(context, "Given API " + string + " is not supported. Supported versions are:" + createSupportedApis());
    }

    private String createSupportedApis() {
        return StringUtils.collectionToDelimitedString(acceptedVersions, ",", "", "");
    }

    @Override
    protected String getValidatorName() {
        return "api version validation";
    }

}
