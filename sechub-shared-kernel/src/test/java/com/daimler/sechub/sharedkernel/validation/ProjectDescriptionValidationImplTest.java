package com.daimler.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ProjectDescriptionValidationImplTest {

	private ProjectDescriptionValidationImpl validationToTest;

	@Before
	public void before() {
		validationToTest = new ProjectDescriptionValidationImpl();
	}

	@Test
	public void an_empty_description_is_valid() {
		assertTrue(validationToTest.validate("").isValid());
	}

	@Test
	public void null_is_invalid() {
		assertFalse(validationToTest.validate((String)null).isValid());
	}

	@Test
	public void a_description_having_exact_171_chars_is_invalid() {
		assertFalse(validationToTest.validate(prepare(171)).isValid());
	}
	@Test
	public void a_description_having_exact_170_chars_is_valid() {
		assertTrue(validationToTest.validate(prepare(170)).isValid());
	}

	private String prepare(int chars) {
		StringBuilder sb = new  StringBuilder();
		for (int i=0;i<chars;i++) {
			sb.append("ü");
		}
		return sb.toString();
	}

}
