// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;

public class AssertMail {

	/**
	 * Assert mail to given test user exists
	 * @param to test user
	 * @param subject subject of mail
	 */
	public static void assertMailExists(TestUser to, String subject) {
		assertMailExists(to.getEmail(),subject);
	}

	/**
	 * Assert that a mail send to administrator email address exist. An admin email
	 * is normally a NPM or a mail distribution address
	 * @param subject subject of mail
	 */
	public static void assertMailToAdminsExists(String subject) {
		assertMailExists("sechub@example.org", subject);
	}

	/**
	 * Assert mail to address exists
	 * @param to mail address
	 * @param subject subject of mail
	 */
	public static void assertMailExists(String to, String subject) {
		IntegrationTestContext.get().emailAccess().findMailOrFail(to,subject);
	}

}
