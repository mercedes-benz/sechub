// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import com.mercedesbenz.sechub.integrationtest.internal.IntegrationTestContext;
import com.mercedesbenz.sechub.integrationtest.internal.MockEmailAccess;

public class AssertMail {

    /**
     * Assert mail to given test user exists
     *
     * @param to      test user
     * @param subject subject of mail
     */
    public static void assertMailExists(TestUser to, String subject) {
        assertMailExists(to.getEmail(), subject);
    }

    /**
     * Assert that a mail send to administrator email address exist. An admin email
     * is normally a NPM or a mail distribution address
     *
     * @param subject subject of mail
     */
    public static void assertMailToAdminsExists(String subject) {
        assertMailExists("int-test_superadmins_npm@example.org", subject);
    }

    /**
     * Assert mail to address exists
     *
     * @param to      mail address
     * @param subject subject of mail
     */
    public static void assertMailExists(String to, String subject) {
        assertMailExists(to, subject, TextSearchMode.EXACT);
    }

    /**
     * Assert mail to given test user exists
     *
     * @param to                test user
     * @param subject           subject of mail
     * @param subjectSearchMode
     */
    public static void assertMailExists(TestUser to, String subject, TextSearchMode subjectSearchMode) {
        assertMailExists(to.getEmail(), subject, subjectSearchMode);
    }

    /**
     * Assert that a mail send to administrator email address exist. An admin email
     * is normally a NPM or a mail distribution address
     *
     * @param subject           subject of mail
     * @param subjectSearchMode
     */
    public static void assertMailToAdminsExists(String subject, TextSearchMode subjectSearchMode) {
        assertMailExists("int-test_superadmins_npm@example.org", subject, subjectSearchMode);
    }

    /**
     * Assert mail to address exists
     *
     * @param to                mail address
     * @param subject           subject of mail
     * @param subjectSearchMode
     */
    public static void assertMailExists(String to, String subject, TextSearchMode subjectSearchMode) {
        IntegrationTestContext.get().emailAccess().findMailOrFail(to, subject, subjectSearchMode, MockEmailAccess.DEFAULT_TIMEOUT);
    }

}
