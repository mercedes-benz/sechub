// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JSONConverterTest {

	private JSONConverter converterToTest;
	
	@SuppressWarnings("deprecation")
    @Rule
	public ExpectedException expected = ExpectedException.none();

	@Before
	public void before() {
		converterToTest = new JSONConverter();
	}

	@Test
	public void toJSON_test_object_returns_expected_json_string() throws Exception {
		assertEquals("{\"info\":\"test1\"}", converterToTest.toJSON(new JSONConverterTestObject("test1")));
	}

	@Test
	public void fromJSON_correct_json_with_double_quotes_results_in_expected_object() throws Exception {
		/* prepare */
		String json = "{\"info\":\"test1\"}";
		/* execute */
		JSONConverterTestObject result = converterToTest.fromJSON(JSONConverterTestObject.class, json);
		/* test */
		assertEquals("test1", result.getInfo());
	}

	@Test
	public void fromJSON_correct_json_with_single_quotes_results_in_expected_object() throws Exception {
		/* prepare */
		String json = "{'info':'info1'}";
		/* execute */
		JSONConverterTestObject result = converterToTest.fromJSON(JSONConverterTestObject.class, json);

		/* test */
		assertNotNull(result);
		assertEquals("info1", result.getInfo());
	}

	@Test
	public void fromJSON_when_string_null_throws_JSONConverterException() throws Exception {
		/* prepare test */
		expected.expect(JSONConverterException.class);
		/* execute */
		converterToTest.fromJSON(JSONConverterTestObject.class, null);
	}

	@Test
	public void fromJSON_comments_are_allowed() throws Exception {
		/* prepare */
		String json = "//just a comment\\\n{\n//comments are a nice thing. \n/*not standard but used in *wildness* so we provide it*/\n'info':'info1'}";
		/* execute */
		JSONConverterTestObject result = converterToTest.fromJSON(JSONConverterTestObject.class, json);

		/* test */
		assertNotNull(result);
		assertEquals("info1", result.getInfo());
	}

}
