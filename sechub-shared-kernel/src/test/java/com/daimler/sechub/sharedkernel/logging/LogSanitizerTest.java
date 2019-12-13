package com.daimler.sechub.sharedkernel.logging;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.sharedkernel.logging.LogSanitizer;

public class LogSanitizerTest {
	private LogSanitizer sanitizerToTest;

	@Before
	public void before() {
		sanitizerToTest = new LogSanitizer();
	}


	@Test
	public void empty_keeps_empty() {
		assertEquals("", sanitizerToTest.sanitize("", -1));
	}

	@Test
	public void null_keeps_null() {
		assertEquals(null, sanitizerToTest.sanitize(null, -1));
		assertEquals(null, sanitizerToTest.sanitize(null, 11));
		assertEquals(null, sanitizerToTest.sanitize(null, 0));
	}

	@Test
	public void object_with_to_string_containing_newLinew_will_be_sanitized() {
		assertEquals("§", sanitizerToTest.sanitize(new TestLogObject("\n"), -1));
	}

	@Test
	public void object_with_to_string_containing_aaa_max_n1_will_NOT_be_sanitized() {
		assertEquals("aaa", sanitizerToTest.sanitize(new TestLogObject("aaa"), -1));
	}

	@Test
	public void object_with_to_string_containing_aaa_max_2_will_be_sanitized_to_aa() {
		assertEquals("aa", sanitizerToTest.sanitize(new TestLogObject("aaa"), 2));
	}

	@Test
	public void text_is_not_shortend_when_max_smaller_than_1() {
		assertEquals("I am full here", sanitizerToTest.sanitize("I am full here", -1));
		assertEquals("I am full here", sanitizerToTest.sanitize("I am full here", 0));
		assertEquals("I am full here", sanitizerToTest.sanitize("I am full here", -3331));
	}


	@Test
	public void text_longer_than_max_is_shortend_to_maximum() {
		assertEquals("I am reduced", sanitizerToTest.sanitize("I am reduced in my length", "I am reduced".length()));
		assertEquals("I", sanitizerToTest.sanitize("I am reduced in my length", 1));
	}

	@Test
	public void text_is_not_shortend_when_text_is_lower_than_50_and_50_is_set_as_maxlength() {
		assertEquals("I am not reduced", sanitizerToTest.sanitize("I am not reduced", 50));
	}


	@Test
	public void tabs_are_replaced() {
		assertEquals("§", sanitizerToTest.sanitize("\t", -1));
		assertEquals("§notab", sanitizerToTest.sanitize("\tnotab", -1));
		assertEquals("§notab§notab2", sanitizerToTest.sanitize("\tnotab\tnotab2", -1));
	}

	@Test
	public void newlines_are_replaced() {
		assertEquals("§", sanitizerToTest.sanitize("\n", -1));
		assertEquals("§nonewline", sanitizerToTest.sanitize("\nnonewline", -1));
		assertEquals("line1§line2§line3", sanitizerToTest.sanitize("line1\nline2\nline3", -1));
	}

	@Test
	public void line1_newline_somethingelse_reduced_to_6_returns_line1_() {
		assertEquals("line1§", sanitizerToTest.sanitize("line1\nsomethingelse", 6));
	}

	@Test
	public void crs_are_replaced() {
		assertEquals("§", sanitizerToTest.sanitize("\r", -1));
		assertEquals("§cr", sanitizerToTest.sanitize("\rcr", -1));
		assertEquals("cr§cr§cr", sanitizerToTest.sanitize("cr\rcr\rcr", -1));
	}

	private class TestLogObject{

		private String asString;

		private TestLogObject(String asString) {
			this.asString=asString;
		}
		@Override
		public String toString() {
			return asString;
		}
	}

}
