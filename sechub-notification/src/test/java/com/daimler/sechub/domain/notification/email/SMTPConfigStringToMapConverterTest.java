// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.email;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

public class SMTPConfigStringToMapConverterTest {

	 private SMTPConfigStringToMapConverter converterToTest;

	@Before
	 public void before() {
		converterToTest = new SMTPConfigStringToMapConverter();
	 }

	@Test
	public void null_results_in_empty_map() {
		/* execute */
		Map<String, String> map = converterToTest.convertToMap(null);

		/* test */
		assertNotNull(map);
		assertTrue(map.isEmpty());
	}

	@Test
	public void empty_results_in_empty_map() {
		/* execute */
		Map<String, String> map = converterToTest.convertToMap("");

		/* test */
		assertNotNull(map);
		assertTrue(map.isEmpty());
	}

	@Test
	public void one_entry_all_correct_set() {
		/* execute */
		Map<String, String> map = converterToTest.convertToMap("mail.smtp.auth=false");

		/* test */
		assertConfig(map).with("mail.smtp.auth", "false");
	}

	@Test
	public void one_entry_all_correct_set_but_spaces() {
		/* execute */
		Map<String, String> map = converterToTest.convertToMap("mail.smtp.auth = false");

		/* test */
		assertConfig(map).with("mail.smtp.auth", "false");
	}

	@Test
	public void two_entry_all_correct_set_but_spaces() {
		/* execute */
		Map<String, String> map = converterToTest.convertToMap("mail.smtp.auth = false,mail.smtp.auth.mechanism=PLAIN");

		/* test */
		assertConfig(map).with("mail.smtp.auth", "false").with("mail.smtp.auth.mechanism", "PLAIN");
	}

	@Test
	public void one_entry_no_value() {
		/* execute */
		Map<String, String> map = converterToTest.convertToMap("mail.smtp.auth=");

		/* test */
		assertConfig(map).without("mail.smtp.auth");
	}

	private AssertConfigResult assertConfig(Map<String, String> map) {
		return new AssertConfigResult(map);
	}

	private class AssertConfigResult{
		private Map<String, String> map;

		private AssertConfigResult(Map<String,String> map) {
			Objects.requireNonNull(map);
			this.map=map;
		}

		public AssertConfigResult with(String key, String value) {
			String found = map.get(key);
			assertEquals("Content differs for key:"+key,value, found);
			return this;
		}
		public AssertConfigResult without(String key) {
			assertFalse("Key does exist:"+key, map.containsKey(key));
			return this;
		}
	}


}
