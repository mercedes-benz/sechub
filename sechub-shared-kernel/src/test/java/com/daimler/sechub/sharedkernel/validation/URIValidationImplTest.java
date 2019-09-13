package com.daimler.sechub.sharedkernel.validation;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

public class URIValidationImplTest {

	private URIValidationImpl validationToTest;

	@Before
	public void before() {
		validationToTest = new URIValidationImpl();
	}

	@Test
	public void null_not_allowed() {
		/* prepare*/
		URI uri = null;

		/* execute */
		ValidationResult result = validationToTest.validate(uri);

		/* test */
		assertFalse(result.isValid());
	}

	@Test
	public void empty_URI_not_allowed() throws Exception {
		/* prepare*/
		URI uri = new URI("");

		/* execute */
		ValidationResult result = validationToTest.validate(uri);

		/* test */
		assertFalse(result.isValid());
	}

	@Test
	public void http_www_google_de_is_allowed() throws Exception {
		/* prepare*/
		URI uri = new URI("http://www.google.de");

		/* execute */
		ValidationResult result = validationToTest.validate(uri);

		/* test */
		assertTrue(result.isValid());
	}

	@Test
	public void https_www_google_de_is_allowed() throws Exception {
		/* prepare*/
		URI uri = new URI("https://www.google.de");

		/* execute */
		ValidationResult result = validationToTest.validate(uri);

		/* test */
		assertTrue(result.isValid());
	}

	@Test
	public void ip_192_168_178_24_is_allowed() throws Exception {
		/* prepare*/
		URI uri = new URI("192.168.178.24");

		/* execute */
		ValidationResult result = validationToTest.validate(uri);

		/* test */
		assertTrue(result.isValid());
	}

	@Test
	public void ip_192_168_178_24_slash_test_is_allowed() throws Exception {
		/* prepare*/
		URI uri = new URI("192.168.178.24/test");

		/* execute */
		ValidationResult result = validationToTest.validate(uri);

		/* test */
		assertTrue(result.isValid());
	}

}
