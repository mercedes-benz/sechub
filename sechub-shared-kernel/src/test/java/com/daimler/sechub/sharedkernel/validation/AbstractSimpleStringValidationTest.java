// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

public class AbstractSimpleStringValidationTest {

	
	private TestAbstractSimpleStringValidation validation;

	@Before
	public void before() {
		validation = new TestAbstractSimpleStringValidation();
	}
	
	@Test
	public void has_same_length_when_trimmed_works()	 throws Exception {

		/* prepare */
		

		/* execute */
		/* test */
		assertTrue(aString("test").validatedBy(validation::validateSameLengthWhenTrimmed).isValid());
		assertTrue(aString("").validatedBy(validation::validateSameLengthWhenTrimmed).isValid());
		
		assertFalse(aString(" test").validatedBy(validation::validateSameLengthWhenTrimmed).isValid());
		assertFalse(aString(" test    ").validatedBy(validation::validateSameLengthWhenTrimmed).isValid());
		assertFalse(aString(" test ").validatedBy(validation::validateSameLengthWhenTrimmed).isValid());
		assertFalse(aString("test ").validatedBy(validation::validateSameLengthWhenTrimmed).isValid());

	}
	
	
	@Test
	public void hasNoUpperCaseCharacters_works()	 throws Exception {

		/* prepare */
		

		/* execute */
		/* test */
		assertTrue(aString("scenario1_user1").validatedBy(validation::validateNoUpperCaseCharacters).isValid());
		assertFalse(aString("scenario1_User1").validatedBy(validation::validateNoUpperCaseCharacters).isValid());

	}

	@Test
	public void when_min_max_is_0_even_an_empty_string_is_valid_length() {
		/* prepare */
		
		validation.access.minLength = 0;
		validation.access.maxLength = 0;
		/* execute + test */
		assertTrue(aString("").validatedBy(validation::validateLength).isValid());
	}

	@Test
	public void when_nothing_defined_an_empty_string_is_valid_length() {
		/* prepare */
		
		/* execute + test */
		assertTrue(aString("").validatedBy(validation::validateLength).isValid());
	}

	@Test
	public void when_nothing_defined_a_null_string_is_valid() {
		/* prepare */
		
		/* execute + test */
		assertTrue(aString(null).validatedBy(validation::validateLength).isValid());
	}
	@Test
	public void when_min_is_1_a_null_string_is_not_valid() {
		/* prepare */
		validation.access.minLength=1;
		
		/* execute + test */
		assertFalse(aString(null).validatedBy(validation::validateLength).isValid());
	}

	@Test
	public void when_min_is_1_max_is_0_an_empty_string_is_NOT_valid() {
		/* prepare */
		
		validation.access.minLength = 1;
		validation.access.maxLength = 0;

		/* execute + test */
		assertFalse(aString("").validatedBy(validation::validateLength).isValid());
	}

	@Test
	public void when_min_is_3_max_is_5_an_string_ab_is_NOT_valid() {
		/* prepare */
		
		validation.access.minLength = 3;
		validation.access.maxLength = 5;

		/* execute + test */
		assertFalse(aString("ab").validatedBy(validation::validateLength).isValid());
	}

	@Test
	public void when_min_is_3_max_is_5_an_string_abcdef_is_NOT_valid() {
		/* prepare */
		
		validation.access.minLength = 3;
		validation.access.maxLength = 5;

		/* execute + test */
		assertFalse(aString("abcdef").validatedBy(validation::validateLength).isValid());
	}

	@Test
	public void when_min_is_3_max_is_5_an_string_abc_is_valid() {
		/* prepare */
		
		validation.access.minLength = 3;
		validation.access.maxLength = 5;

		/* execute + test */
		assertTrue(aString("abc").validatedBy(validation::validateLength).isValid());
	}

	@Test
	public void when_min_is_3_max_is_5_an_string_abcd_is_valid() {
		/* prepare */
		
		validation.access.minLength = 3;
		validation.access.maxLength = 5;

		/* execute + test */
		assertTrue(aString("abcd").validatedBy(validation::validateLength).isValid());
	}

	@Test
	public void when_min_is_0_max_is_1_an_empty_string_is_valid() {
		/* prepare */
		
		validation.access.minLength = 0;
		validation.access.maxLength = 1;

		/* execute + test */
		assertTrue(aString("").validatedBy(validation::validateLength).isValid());
	}
	private StringValidationTester aString(String string){
		return new StringValidationTester(string);
	}
	
	private class StringValidationTester{

		private ValidationContext<String> context;

		public StringValidationTester(String target) {
			context=new ValidationContext<String>(target);
		}

		public boolean isValid() {
			return this.context.result.valid;
		}
		
		public StringValidationTester validatedBy(Consumer<ValidationContext<String>> voidFunction) {
			voidFunction.accept(context);
			return this;
		}
	}
	
	public class TestAbstractSimpleStringValidation extends AbstractSimpleStringValidation {

		private ValidationConfig access;

		@Override
		protected void setup(ValidationConfig config) {
			/*
			 * we just mark the normally inaccessible config so accessible in testcase again
			 */
			this.access = config;
		}

		@Override
		protected void validate(ValidationContext<String> context) {
			// do nothing in test case - we test the parts standalone
			
		}

	}
}
