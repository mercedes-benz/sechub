package com.mercedesbenz.sechub.commons.model.template;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.core.CachingPatternProvider;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariable;
import com.mercedesbenz.sechub.commons.model.template.TemplateDefinition.TemplateVariableValidation;
import com.mercedesbenz.sechub.commons.model.template.TemplateUsageValidator.TemplateUsageValidatorResult;

class TemplateUsageValidatorTest {

    private TemplateUsageValidator validatorToTest;

    private CachingPatternProvider cachingPatternProvider;

    @BeforeEach
    void beforeEach() {
        cachingPatternProvider = mock();
        validatorToTest = new TemplateUsageValidator(cachingPatternProvider);
    }

    @Test
    void null_template_data_throws_exception() {
        /* prepare */
        TemplateDefinition definition = new TemplateDefinition();
        TemplateData data = null;

        /* execute + test */
        assertThatThrownBy(() -> validatorToTest.validate(definition, data)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("data may not be null");

    }

    @Test
    void null_template_definition_throws_exception() {
        /* prepare */
        TemplateDefinition definition = null;
        TemplateData data = new TemplateData();

        /* execute + test */
        assertThatThrownBy(() -> validatorToTest.validate(definition, data)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("definition may not be null");

    }

    @Test
    void template_definition_has_no_variables_but_data_contains_variables() {
        /* prepare */
        TemplateDefinition definition = new TemplateDefinition();
        TemplateData data = new TemplateData();
        data.getVariables().put("i-am-here-but-not-necessary", "value1");

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isTrue(); // unused variable is just ignored...
        assertThat(result.getMessage()).isNull();
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = { "mandatory-var2", "mandatory-va", "mandatory_var" })
    void mandatory_variable_not_defined_in_template_data(String nameOfOtherVariable) {
        /* prepare */
        String variableName = "mandatory-var";
        TemplateDefinition definition = createTestTemplateWithMandatoryVariable(variableName);

        TemplateData data = new TemplateData();
        if (nameOfOtherVariable != null) {
            data.getVariables().put(nameOfOtherVariable, "some value");
        }

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains(variableName, "not defined");
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { "mandatory-var", "other" })
    void mandatory_variable_is_defined_in_template_data(String variableName) {
        /* prepare */
        TemplateDefinition definition = createTestTemplateWithMandatoryVariable(variableName);

        TemplateData data = new TemplateData();
        data.getVariables().put(variableName, "some value");

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isNull();
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = { "optional-var2", "optional-va", "optional_var" })
    void optional_variable_not_defined_in_template_data(String nameOfOtherVariable) {
        /* prepare */
        String variableName = "optional-var";
        TemplateDefinition definition = createTestTemplateWithOptionalVariable(variableName);

        TemplateData data = new TemplateData();
        if (nameOfOtherVariable != null) {
            data.getVariables().put(nameOfOtherVariable, "some value");
        }

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isNull();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { "optional-var", "other" })
    void optional_variable_is_defined_in_template_data(String variableName) {
        /* prepare */
        TemplateDefinition definition = createTestTemplateWithOptionalVariable(variableName);

        TemplateData data = new TemplateData();
        data.getVariables().put(variableName, "some value");

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(MinLengthFailingProvider.class)
    void validation_min_length_failure(boolean optional, String value, int minLength) {
        /* prepare */
        String variableName = "variable-x";
        TemplateDefinition definition = createTestTemplateWithVariable(variableName, optional);
        TemplateVariableValidation validation = firstVariableValidation(definition);
        validation.setMinLength(minLength);

        TemplateData data = new TemplateData();
        data.getVariables().put(variableName, value);

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("Variable", "variable-x", "less than " + minLength);
    }

    @ParameterizedTest
    @ArgumentsSource(MinLengthValidProvider.class)
    void validation_min_length_valid(boolean optional, String value, int minLength) {
        /* prepare */
        String variableName = "variable-x";
        TemplateDefinition definition = createTestTemplateWithVariable(variableName, optional);
        TemplateVariableValidation validation = firstVariableValidation(definition);
        validation.setMinLength(minLength);

        TemplateData data = new TemplateData();
        data.getVariables().put(variableName, value);

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(MaxLengthFailingProvider.class)
    void validation_max_length_failure(boolean optional, String value, int maxLength) {
        /* prepare */
        String variableName = "variable-x";
        TemplateDefinition definition = createTestTemplateWithVariable(variableName, optional);
        TemplateVariableValidation validation = firstVariableValidation(definition);
        validation.setMaxLength(maxLength);

        TemplateData data = new TemplateData();
        data.getVariables().put(variableName, value);

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("Variable", "variable-x", "greater than " + maxLength);
    }

    @ParameterizedTest
    @ArgumentsSource(MaxLengthValidProvider.class)
    void validation_max_length_valid(boolean optional, String value, int maxLength) {
        /* prepare */
        String variableName = "variable-x";
        TemplateDefinition definition = createTestTemplateWithVariable(variableName, optional);
        TemplateVariableValidation validation = firstVariableValidation(definition);
        validation.setMaxLength(maxLength);

        TemplateData data = new TemplateData();
        data.getVariables().put(variableName, value);

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(RegExpressionFailingProvider.class)
    void validation_regexp_failure(boolean optional, String value, String regularExpression) {
        /* prepare */
        String variableName = "variable-regex-failure";
        TemplateDefinition definition = createTestTemplateWithVariable(variableName, optional);
        TemplateVariableValidation validation = firstVariableValidation(definition);
        validation.setRegularExpression(regularExpression);

        when(cachingPatternProvider.get(regularExpression)).thenReturn(Pattern.compile(regularExpression)); // we just do not cache inside the tests

        TemplateData data = new TemplateData();
        data.getVariables().put(variableName, value);

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("Variable", "variable-regex-failure", "must have a value which matches regular expression", regularExpression);
    }

    @ParameterizedTest
    @ArgumentsSource(RegExpressionValidProvider.class)
    void validation_regexp_valid(boolean optional, String value, String regularExpression) {
        /* prepare */
        String variableName = "variable-regex-valid";
        TemplateDefinition definition = createTestTemplateWithVariable(variableName, optional);
        TemplateVariableValidation validation = firstVariableValidation(definition);
        validation.setRegularExpression(regularExpression);

        when(cachingPatternProvider.get(regularExpression)).thenReturn(Pattern.compile(regularExpression)); // we just do not cache inside the tests

        TemplateData data = new TemplateData();
        data.getVariables().put(variableName, value);

        /* execute */
        TemplateUsageValidatorResult result = validatorToTest.validate(definition, data);

        /* test */
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isNull();
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ................Helper.......................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */

    private TemplateVariableValidation firstVariableValidation(TemplateDefinition definition) {
        TemplateVariable firstVariable = definition.getVariables().iterator().next();
        if (firstVariable == null) {
            throw new IllegalStateException("test case corrupt!");
        }
        TemplateVariableValidation validation = firstVariable.getValidation();
        if (validation == null) {
            validation = new TemplateVariableValidation();
            firstVariable.setValidation(validation);
        }
        return validation;
    }

    private TemplateDefinition createTestTemplateWithOptionalVariable(String name) {
        return createTestTemplateWithVariable(name, true);
    }

    private TemplateDefinition createTestTemplateWithMandatoryVariable(String name) {
        return createTestTemplateWithVariable(name, false);
    }

    private TemplateDefinition createTestTemplateWithVariable(String name, boolean optional) {
        TemplateDefinition definition = new TemplateDefinition();
        TemplateVariable variable = new TemplateVariable();
        variable.setName(name);
        variable.setOptional(optional);
        definition.getVariables().add(variable);
        return definition;
    }

    private static class MinLengthFailingProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
              Arguments.of(false, "1234", 5),
              Arguments.of(false, "1234", 10),
              Arguments.of(false, "ab", 3),

              Arguments.of(true, "1234", 5),
              Arguments.of(true, "1234", 10),
              Arguments.of(true, "ab", 3));
        }
        /* @formatter:on*/
    }

    private static class MinLengthValidProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(false, "1234", 4),
                    Arguments.of(false, "1234", 2),
                    Arguments.of(false, "ab", 2),

                    Arguments.of(true, "1234", 4),
                    Arguments.of(true, "1234", 2),
                    Arguments.of(true, "ab", 2));
        }
        /* @formatter:on*/
    }

    private static class MaxLengthFailingProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(false, "1234", 3),
                    Arguments.of(false, "123456789", 8),
                    Arguments.of(false, "ab", 1),

                    Arguments.of(true, "1234", 3),
                    Arguments.of(true, "123456789", 8),
                    Arguments.of(true, "ab", 1));
        }
        /* @formatter:on*/
    }

    private static class MaxLengthValidProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(false, "1234", 4),
                    Arguments.of(false, "123456789", 9),
                    Arguments.of(false, "123456789", 10),
                    Arguments.of(false, "ab", 2),

                    Arguments.of(true, "1234", 4),
                    Arguments.of(true, "123456789", 9),
                    Arguments.of(true, "123456789", 10),
                    Arguments.of(true, "ab", 2));
        }
        /* @formatter:on*/
    }

    private static class RegExpressionFailingProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(false, "", ".+"),
                    Arguments.of(false, "12x4", "1234"),
                    Arguments.of(false, "text0", "[a-z]*"),

                    Arguments.of(true, "", ".+"),
                    Arguments.of(true, "12x4", "1234"),
                    Arguments.of(true, "text0", "[a-z]*"));
        }
        /* @formatter:on*/
    }

    private static class RegExpressionValidProvider implements ArgumentsProvider {
        /* @formatter:off */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(false, " ", ".+"),
                    Arguments.of(false, "1234", "1234"),
                    Arguments.of(false, "text", "[a-z]*"),

                    Arguments.of(true, " ", ".+"),
                    Arguments.of(true, "1234", "1234"),
                    Arguments.of(true, "text", "[a-z]*"));
        }
        /* @formatter:on*/
    }

}
