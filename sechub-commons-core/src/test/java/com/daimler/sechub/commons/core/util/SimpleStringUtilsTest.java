// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.core.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.Test;

public class SimpleStringUtilsTest {

	@Test
	public void null_equals_null_returns_true() {
		assertThat(SimpleStringUtils.equals(null, null), is(true));
	}
	
	@Test
	public void null_equals_not_empty_strings_returns_false() {
		assertThat(SimpleStringUtils.equals(null, "string"), is(false));
		assertThat(SimpleStringUtils.equals(null, ""), is(false));
		assertThat(SimpleStringUtils.equals("",null), is(false));
		assertThat(SimpleStringUtils.equals("string",null), is(false));
	}
	
	@Test
	public void string1_equals_string2_returns_false() {
		assertThat(SimpleStringUtils.equals("string1", "string2"), is(false));
	}
	
	@Test
	public void same_strings_equals_returns_true() {
		assertThat(SimpleStringUtils.equals("string1", "string1"), is(true));
		assertThat(SimpleStringUtils.equals("", ""), is(true));
	}
	
	@Test
	public void string1_is_part_of_string2_returns_false() {
		assertThat(SimpleStringUtils.startsWith("string1", "string2"), is(false));
		assertThat(SimpleStringUtils.startsWith("string2", "string1"), is(false));
	}
	
	@Test
	public void string1_is_part_of_string1a_returns_true() {
		assertThat(SimpleStringUtils.startsWith("string1", "string1a"), is(true));
	}
	@Test
	public void string1a_is_part_of_string1_returns_false() {
		assertThat(SimpleStringUtils.startsWith("string1a", "string1"), is(false));
	}
	
	@Test
	public void string1_is_part_of_string1_returns_true() {
		assertThat(SimpleStringUtils.startsWith("string1", "string1a"), is(true));
	}


}
