// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.template;

import static java.util.Objects.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.mercedesbenz.sechub.commons.core.CachingPatternProvider;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariableValidation;

public class TemplateUsageValidator {

    private CachingPatternProvider cachingPatternProvider;

    public TemplateUsageValidator(CachingPatternProvider cachingPatternProvider) {
        this.cachingPatternProvider = requireNonNull(cachingPatternProvider, "caching pattern provider may not be null!");
    }

    /**
     * Validates that given template data contains variables for given template
     * definition in expected way.
     *
     * @param definition template definition
     * @param data       the template data from a SecHub configuration file
     * @return result, never <code>null</code>
     */
    public TemplateUsageValidatorResult validate(TemplateDefinition definition, TemplateData data) {
        requireNonNull(definition, "Template definition may not be null!");
        requireNonNull(data, "Template data may not be null!");

        try {
            startAssertions(definition, data);
        } catch (TemplateUsageAssertionException exception) {
            return validationFailure(exception.getMessage());
        }
        return validationOk();
    }

    private void startAssertions(TemplateDefinition definition, TemplateData data) throws TemplateUsageAssertionException {
        List<TemplateVariable> variables = definition.getVariables();
        Map<String, String> variablesData = data.getVariables();

        assertMandatoryVariablesAvailable(variables, variablesData);

        /* validation check */
        for (TemplateVariable variable : variables) {
            assertVariableValidation(variable, data);
        }
    }

    private void assertVariableValidation(TemplateVariable variable, TemplateData data) throws TemplateUsageAssertionException {
        TemplateVariableValidation validation = variable.getValidation();
        if (validation == null) {
            return;
        }
        String variableName = variable.getName();
        String variableValue = data.getVariables().get(variableName);
        if (variableValue == null) {
            if (variable.isOptional()) {
                return;
            }
            throw new TemplateUsageAssertionException("Variable " + variableName + " is mandatory but null");
        }

        assertMinLength(validation, variableName, variableValue);
        assertMaxLength(validation, variableName, variableValue);
        assertRegularExpression(validation, variableName, variableValue);
    }

    private void assertMinLength(TemplateVariableValidation validation, String variableName, String variableValue) throws TemplateUsageAssertionException {
        Integer minLength = validation.getMinLength();
        if (minLength == null) {
            return;
        }
        if (variableValue.length() < minLength) {
            throw new TemplateUsageAssertionException("Variable " + variableName + " length less than " + minLength);
        }
    }

    private void assertMaxLength(TemplateVariableValidation validation, String variableName, String variableValue) throws TemplateUsageAssertionException {
        Integer maxLength = validation.getMaxLength();
        if (maxLength == null) {
            return;
        }
        if (variableValue.length() > maxLength) {
            throw new TemplateUsageAssertionException("Variable " + variableName + " length greater than " + maxLength);
        }
    }

    private void assertRegularExpression(TemplateVariableValidation validation, String variableName, String variableValue)
            throws TemplateUsageAssertionException {
        String regularExpression = validation.getRegularExpression();
        if (regularExpression == null || regularExpression.isBlank()) {
            return;
        }

        Pattern pattern = cachingPatternProvider.get(regularExpression);
        if (pattern.matcher(variableValue).matches()) {
            return;
        }
        throw new TemplateUsageAssertionException(
                "Variable " + variableName + " must have a value which matches regular expression '" + regularExpression + "'");
    }

    private void assertMandatoryVariablesAvailable(List<TemplateVariable> variablesDefined, Map<String, String> variablesInData)
            throws TemplateUsageAssertionException {
        /* mandatory check */
        for (TemplateVariable variableDefined : variablesDefined) {
            if (variableDefined.isOptional()) {
                continue;
            }
            /* is mandatory */
            String nameOfMandatoryVariable = variableDefined.getName();
            if (variablesInData.containsKey(nameOfMandatoryVariable)) {
                continue;
            }
            /* not found */
            throw new TemplateUsageAssertionException("The mandatory variable '" + nameOfMandatoryVariable + "' is not defined in template data!");
        }
    }

    private TemplateUsageValidatorResult validationOk() {
        TemplateUsageValidatorResult result = new TemplateUsageValidatorResult();
        result.valid = true;
        return result;
    }

    private TemplateUsageValidatorResult validationFailure(String message) {
        TemplateUsageValidatorResult result = new TemplateUsageValidatorResult();
        result.valid = false;
        result.message = message;
        return result;
    }

    private class TemplateUsageAssertionException extends Exception {

        private static final long serialVersionUID = 1L;

        private TemplateUsageAssertionException(String message) {
            super(message);
        }

    }

    public class TemplateUsageValidatorResult {
        private boolean valid;
        private String message;

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
