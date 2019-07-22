// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;

public class AssertMail {

	public static void assertMailExists(TestUser to, String subject) {
		assertMailExists(to.getEmail(),subject);
	}
	public static void assertMailExists(String to, String subject) {
		IntegrationTestContext.get().emailAccess().findMailOrFail(to,subject);
	}
}
