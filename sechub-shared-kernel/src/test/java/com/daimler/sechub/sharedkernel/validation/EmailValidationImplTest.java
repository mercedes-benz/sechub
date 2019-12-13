package com.daimler.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class EmailValidationImplTest {

	private EmailValidationImpl validationToTest;

	@Before
	public void before() {
		validationToTest = new EmailValidationImpl();
	}

	@Test
	public void somebody_at_gmail_adress_is_valid() {
		assertTrue(validationToTest.validate("somebody@gmail.com").isValid());
	}

	@Test
	public void null_is_invalid() {
		assertFalse(validationToTest.validate((String)null).isValid());
	}

	@Test
	public void empty_is_invalid() {
		assertFalse(validationToTest.validate("").isValid());
	}

}
