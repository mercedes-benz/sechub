// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils.*;

public abstract class AbstractSimpleStringValidation extends AbstractValidation<String> {

    protected void validateNoUpperCaseCharacters(ValidationContext<String> context) {
        String string = context.objectToValidate;
        if (string == null) {
            return;
        }
        if (string.isEmpty()) {
            return;
        }
        for (char c : string.toCharArray()) {
            if (Character.isUpperCase(c)) {
                addErrorMessage(context, "Uppercase character not allowed, but found '" + c + "'.");
                return;
            }
        }
    }

    final protected void validateWithoutWhitespaces(ValidationContext<String> context) {
        String string = context.objectToValidate;
        if (string == null) {
            return;
        }
        if (string.isEmpty()) {
            return;
        }
        for (char c : string.toCharArray()) {
            if (Character.isWhitespace(c)) {
                addErrorMessage(context, "Whitespace detected inside string");
                return;
            }
        }
    }

    protected void validateOnlyAlphabeticDigitOrAllowedParts(ValidationContext<String> context, char... alsoAllowed) {
        String string = context.objectToValidate;
        if (hasStandardAsciiLettersDigitsOrAdditionalAllowedCharacters(string, alsoAllowed)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("The string '");
        sb.append(string);
        sb.append("' contains at least one character being neither a digit, alphabetical");
        if (alsoAllowed.length > 0) {
            sb.append("or ");
            for (int i = 0; i < alsoAllowed.length; i++) {
                if (i != 0) {
                    sb.append(',');
                }
                sb.append(alsoAllowed[i]);
            }
            sb.append("]");
        }
        addErrorMessage(context, sb.toString());
    }

    protected void validateNotContainingCharackters(ValidationContext<String> context, char... chars) {
        String string = context.objectToValidate;
        if (string == null) {
            return;
        }
        if (string.isEmpty()) {
            return;
        }
        for (char c : string.toCharArray()) {
            for (char denied : chars) {
                if (c == denied) {
                    addErrorMessage(context, "Character now not allowed here, but found '" + c + "'.");
                    return;
                }
            }
        }
    }

    protected void validateSameLengthWhenTrimmed(ValidationContext<String> context) {
        String string = context.objectToValidate;
        if (string == null) {
            return;
        }
        String trimmedName = string.trim();
        boolean sameLength = trimmedName.length() == string.length();
        if (sameLength) {
            return;
        }
        addErrorMessage(context, "Please remove whitespaces at beginning and at the end");
    }

    /**
     * Validates minimum and maximum length
     *
     * @param context
     */
    protected void validateLength(ValidationContext<String> context) {
        validateMinLength(context);
        validateMaxLength(context);
    }

    protected void validateMaxLength(ValidationContext<String> context) {
        String string = context.objectToValidate;
        if (getMaxLength() <= 0) {
            return;
        }
        if (string == null) {
            addErrorMessage(context, "String is null, so max length " + getMaxLength() + " not valid");
            return;
        }
        boolean validMaxLength = string.length() <= getMaxLength();
        if (!validMaxLength) {
            addErrorMessage(context, "Maximum size is " + getMaxLength() + " but was " + string.length());
        }
    }

    protected void validateMinLength(ValidationContext<String> context) {
        if (getMinLength() <= 0) {
            return;
        }
        String string = context.objectToValidate;
        if (string == null) {
            addErrorMessage(context, "String is null, so min length " + getMinLength() + " not valid");
            return;
        }
        boolean validMinLength = string.length() >= getMinLength();
        if (!validMinLength) {
            addErrorMessage(context, "Minimum size is " + getMinLength() + " but was " + string.length());
        }
    }
}
