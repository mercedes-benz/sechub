// SPDX-License-Identifier: MIT
package com.daimler.sechub.docgen.spring;

import static org.junit.Assert.*;

import org.junit.Test;

public class ListedProfileTest {

	@Test
	public void calculateProfileName() {
		assertEquals("unknown",ListedProfile.calculateProfileName("application-dev"));
		assertEquals("unknown-test-dev",ListedProfile.calculateProfileName("test-dev.properteis"));

		assertEquals("dev",ListedProfile.calculateProfileName("application-dev.properties"));
		assertEquals("dev",ListedProfile.calculateProfileName("application-dev.yml"));
		assertEquals("dev",ListedProfile.calculateProfileName("application-dev.yaml"));

		assertEquals("",ListedProfile.calculateProfileName("application.properties"));
		assertEquals("",ListedProfile.calculateProfileName("application.yml"));
		assertEquals("",ListedProfile.calculateProfileName("application.yaml"));
	}

}
