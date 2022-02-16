// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import java.util.function.Consumer;

import org.junit.Test;

public class ApiVersionValidationImplTest {

    @Test
    public void api_1_0_defined_so_1_0_is_valid_but_not_2_0_or_3_0() {
        ApiVersionValidationImpl validation = new ApiVersionValidationImpl();
        assertTrue(aString("1.0").validatedBy(validation::validateSupportedVersion).isValid());
        assertFalse(aString("2.0").validatedBy(validation::validateSupportedVersion).isValid());
        assertFalse(aString("3.0").validatedBy(validation::validateSupportedVersion).isValid());
    }

    @Test
    public void api_1_0_and_2_0_defined_so_1_0_and_2_0_are_valid_but_not_3_0() {
        ApiVersionValidationImpl validation = new ApiVersionValidationImpl(new String[] { "1.0", "2.0" });
        assertTrue(aString("1.0").validatedBy(validation::validateSupportedVersion).isValid());
        assertTrue(aString("2.0").validatedBy(validation::validateSupportedVersion).isValid());
        assertFalse(aString("3.0").validatedBy(validation::validateSupportedVersion).isValid());
    }

    private StringValidationTester aString(String string) {
        return new StringValidationTester(string);
    }

    private class StringValidationTester {

        private ValidationContext<String> context;

        public StringValidationTester(String target) {
            context = new ValidationContext<String>(target);
        }

        public boolean isValid() {
            return this.context.result.valid;
        }

        public StringValidationTester validatedBy(Consumer<ValidationContext<String>> voidFunction) {
            voidFunction.accept(context);
            return this;
        }
    }

}
