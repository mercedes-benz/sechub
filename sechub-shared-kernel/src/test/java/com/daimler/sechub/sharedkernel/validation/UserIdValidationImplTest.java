// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserIdValidationImplTest {

	private UserIdValidationImpl userIdValidation = new UserIdValidationImpl();

	@Test
	public void lengthTest_5() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxab");
		assertTrue("User id not valid", userIdValidationResult.isValid());
	}

	@Test
	public void lengthTest_6() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxxab");
		assertTrue("User id not valid", userIdValidationResult.isValid());
	}

	@Test
	public void lengthTest_1() {
		ValidationResult userIdValidationResult = userIdValidation.validate("a");
		assertFalse("User id is not too short.", userIdValidationResult.isValid());
	}

	@Test
	public void lengthTest_0() {
		ValidationResult userIdValidationResult = userIdValidation.validate("");
		assertFalse("User id is not too short.", userIdValidationResult.isValid());
	}

	@Test
	public void ABCDIsNOTValidBecauseUppercaseCharactersNotAllowed() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxxABCD");
		assertFalse("User id is not okay, but should?!?", userIdValidationResult.isValid());
	}

	@Test
	public void xxxabcdIsValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxabcd");
		assertTrue("User id is not okay, but should?!?", userIdValidationResult.isValid());
	}
	@Test
	public void xx_abcdIsValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xx_abcd");
		assertTrue("User id is not okay, but should?!?", userIdValidationResult.isValid());
	}

	@Test
	public void xx_hyphen_abcd_underscoreIsValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xx-abcd");
		assertTrue("User id is not okay, but should?!?", userIdValidationResult.isValid());
	}

	@Test
	public void containsDotIsNotValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxab.d");
		assertFalse("User id dot forbidden, but accepted?!?", userIdValidationResult.isValid());
	}

	@Test
	public void containsSlashIsNotValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxab/d");
		assertFalse("User id slash forbidden, but accepted?!?", userIdValidationResult.isValid());
	}

	@Test
	public void containsBackSlashIsNotValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxab\\d");
		assertFalse("User id backslash forbidden, but accepted?!?", userIdValidationResult.isValid());
	}

	@Test
	public void containsDollorIsNotValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxab$d");
		assertFalse("User id backslash forbidden, but accepted?!?", userIdValidationResult.isValid());
	}

	@Test
	public void containsPercentageIsNotValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxab$d");
		assertFalse("User id backslash forbidden, but accepted?!?", userIdValidationResult.isValid());
	}

	@Test
	public void containsQuestionMarkIsNotValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxab?d");
		assertFalse("User id backslash forbidden, but accepted?!?", userIdValidationResult.isValid());
	}

	@Test
	public void containsExlamationMarkIsNotValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("xxxab!d");
		assertFalse("User id backslash forbidden, but accepted?!?", userIdValidationResult.isValid());
	}

	@Test
	public void containsColonIsNotValid() {
		ValidationResult userIdValidationResult = userIdValidation.validate("AB:D".toLowerCase());
		assertFalse("User id backslash forbidden, but accepted?!?", userIdValidationResult.isValid());
	}

}
