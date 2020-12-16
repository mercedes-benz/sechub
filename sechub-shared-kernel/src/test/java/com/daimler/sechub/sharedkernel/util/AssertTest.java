// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class AssertTest {

	@Rule
	public ExpectedException expected = ExpectedExceptionFactory.none();
	
	@Test
	public void not_null_with_null_throws_illegal_argument() {
		/* prepare test */
		expected.expect(IllegalArgumentException.class);
		
		/* execute */
		Assert.notNull(null, "message");
	}
	
	@Test
	public void not_null_with_null_even_when_message_is_null_throws_illegal_argument() {
		/* prepare test */
		expected.expect(IllegalArgumentException.class);
		
		/* execute */
		Assert.notNull(null, null);
	}
	
	@Test
	public void not_null_with_not_null_throws_no_exception() {
		
		/* execute */
		Assert.notNull("not null...", "message");
	}
	
	@Test
	public void not_null_with_not_null_null_message_throws_no_exception() {
		
		/* execute */
		Assert.notNull("not null...", null);
	}
	
	@Test
	public void not_empty__with_empty_string_throws_illegal_argument() {
		/* prepare test */
		expected.expect(IllegalArgumentException.class);
		
		/* execute */
		Assert.notEmpty("", "message");
	}
	
	public void not_empty__with_null_string_throws_illegal_argument() {
		/* prepare test */
		expected.expect(IllegalArgumentException.class);
		
		/* execute */
		Assert.notEmpty((String)null, "message");
	}
	
	@Test
	public void not_empty__with_null_collection_throws_illegal_argument() {
		/* prepare test */
		expected.expect(IllegalArgumentException.class);
		
		/* execute */
		Assert.notEmpty((Collection<?>)null, "message");
	}
	
	@Test
	public void not_empty__with_empty_array_list_as_collection_throws_illegal_argument() {
		/* prepare test */
		expected.expect(IllegalArgumentException.class);
		
		/* execute */
		Assert.notEmpty(new ArrayList<>(), "message");
	}
	
	@Test
	public void not_empty__with_not_empty_array_list_as_collection_throws_illegal_argument() {
		
		/* execute */
		Assert.notEmpty(Arrays.asList("a value"), "message");
	}
}
