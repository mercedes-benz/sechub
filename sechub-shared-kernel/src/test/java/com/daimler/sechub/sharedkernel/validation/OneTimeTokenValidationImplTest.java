package com.daimler.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class OneTimeTokenValidationImplTest {

	private OneTimeTokenValidationImpl validationToTest;
	private final static String VALID_EXAMPLETOKEN = "NDA2Yjg5NTYtY2I3Yy00MTJhLTg0YjgtZGMwZjdjMDM4NDVj";

	@Before
	public void before() {
		validationToTest = new OneTimeTokenValidationImpl();
	}

	@Test
	public void valid_token_is_valid() {
		assertTrue(validationToTest.validate(VALID_EXAMPLETOKEN).isValid());
	}

	@Test
	public void a_token_containing_only_40_chars_is_valid() {
		/* prepare */
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<40;i++) {
			sb.append('A');
		}
		/* test */
		assertTrue(validationToTest.validate(sb.toString()).isValid());
	}

	@Test
	public void a_token_containing_only_40_underscores_is_invvalid() {
		/* prepare */
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<40;i++) {
			sb.append('_');
		}
		/* test */
		assertFalse(validationToTest.validate(sb.toString()).isValid());
	}

	@Test
	public void a_token_with_length_10_is_invalid() {
		assertFalse(validationToTest.validate("1234567890").isValid());
	}

}
