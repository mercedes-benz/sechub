// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

public class SeverityTest {

	@Test
	public void value_null_is_null() {
		assertNull(Severity.fromString(null));
	}
	
	@Test
	public void value_unknown_value12345_is_null() {
		assertNull(Severity.fromString("unknown_value12345"));
	}

	@Test
	public void value_as_uppercase_is_returned_as_severity() {
		for (Severity severity : Severity.values()) {
			String name = severity.name();

			assertEquals(severity, Severity.fromString(name.toUpperCase()));
		}
	}

	@Test
	public void value_as_lowercase_is_returned_as_severity() {
		for (Severity severity : Severity.values()) {
			String name = severity.name();
			
			assertEquals(severity, Severity.fromString(name.toLowerCase()));
		}
	}

	@Test
	public void value_as_firstUpperCasedThanLowerCase_is_returned_as_severity() {
		for (Severity severity : Severity.values()) {
			String name = severity.name();
			String lowerCase = name.toLowerCase();
			String upperCase = name.toUpperCase();
			String firstUpperCasedThanLowerCase = upperCase.substring(0, 1) + lowerCase.substring(1);

			assertEquals(severity, Severity.fromString(firstUpperCasedThanLowerCase));

		}
	}

}
