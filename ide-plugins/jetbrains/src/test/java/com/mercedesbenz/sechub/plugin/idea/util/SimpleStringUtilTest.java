// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.idea.util;

import static org.junit.Assert.assertEquals;

import com.mercedesbenz.sechub.plugin.util.SimpleStringUtil;
import org.junit.Test;


public class SimpleStringUtilTest {

	@Test
	public void removeAllSpaces_string_with_space_characters() {
		String expected = "HelloWorld!";
		String withSpaces = "\t\t H \t el l o    World! \t";
		
		String actual = SimpleStringUtil.removeAllSpaces(withSpaces);
		
		assertEquals(expected, actual);
	}

}
