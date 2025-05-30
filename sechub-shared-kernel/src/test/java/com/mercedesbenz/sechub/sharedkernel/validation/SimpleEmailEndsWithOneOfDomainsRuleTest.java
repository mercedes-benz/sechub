// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SimpleEmailEndsWithOneOfDomainsRuleTest {

    @Test
    void sechub_email_rule_properties_is_null_throws_exception() {
        /* test */
        assertThatThrownBy(() -> new SimpleEmailEndsWithOneOfDomainsRule(null)).hasMessage("The value of 'sechubEmailRuleProperties' must not be null!");
    }

    @ParameterizedTest
    @ValueSource(strings = { "example@example.com", "example@test.com", "example@other.com", "example@accepted.com" })
    void list_of_accepted_domains_is_empty_accepts_all_and_results_in_no_error(String email) {
        /* prepare */
        SechubEmailRuleProperties properties = new SechubEmailRuleProperties(Collections.emptyList());
        SimpleEmailEndsWithOneOfDomainsRule emailRule = new SimpleEmailEndsWithOneOfDomainsRule(properties);

        ValidationContext<String> context = new ValidationContext<String>(email);

        /* execute */
        emailRule.applyRule(email, context);

        /* test */
        assertThat(context.isInValid()).isFalse();
    }

    @Test
    void email_is_null_does_nothing_and_results_in_no_error() {
        /* prepare */
        SechubEmailRuleProperties properties = new SechubEmailRuleProperties(List.of("example.com"));
        SimpleEmailEndsWithOneOfDomainsRule emailRule = new SimpleEmailEndsWithOneOfDomainsRule(properties);

        ValidationContext<String> context = new ValidationContext<String>("");

        /* execute */
        emailRule.applyRule(null, context);

        /* test */
        assertThat(context.isInValid()).isFalse();
    }

    @Test
    void validation_context_is_null_does_nothing_and_results_in_no_error() {
        /* prepare */
        SechubEmailRuleProperties properties = new SechubEmailRuleProperties(List.of("example.com"));
        SimpleEmailEndsWithOneOfDomainsRule emailRule = new SimpleEmailEndsWithOneOfDomainsRule(properties);

        String email = "example@example.com";
        ValidationContext<String> context = null;

        /* execute */
        emailRule.applyRule(email, context);

        /* test */
        assertThat(context).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "example@example.com", "example@test.com" })
    void accepted_domains_result_in_no_error_in_the_validation_context(String email) {
        /* prepare */
        SechubEmailRuleProperties properties = new SechubEmailRuleProperties(List.of("example.com", "test.com"));
        SimpleEmailEndsWithOneOfDomainsRule emailRule = new SimpleEmailEndsWithOneOfDomainsRule(properties);

        ValidationContext<String> context = new ValidationContext<String>(email);

        /* execute */
        emailRule.applyRule(email, context);

        /* test */
        assertThat(context.isInValid()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "example@other.com", "example@unaccepted.com" })
    void not_accepted_domains_result_in_an_error_in_the_validation_context(String email) {
        /* prepare */
        SechubEmailRuleProperties properties = new SechubEmailRuleProperties(List.of("example.com", "test.com"));
        SimpleEmailEndsWithOneOfDomainsRule emailRule = new SimpleEmailEndsWithOneOfDomainsRule(properties);

        ValidationContext<String> context = new ValidationContext<String>(email);

        /* execute */
        emailRule.applyRule(email, context);

        /* test */
        assertThat(context.isInValid()).isTrue();
        List<String> errors = context.result.getErrors();
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.get(0)).isEqualTo("The email address did not end with any of the allowed domains: %s".formatted(properties.getAllowedDomains()));
    }

}
