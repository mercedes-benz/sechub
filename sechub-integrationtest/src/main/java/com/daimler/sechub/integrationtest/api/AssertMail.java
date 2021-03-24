// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import com.daimler.sechub.integrationtest.internal.IntegrationTestContext;
import com.daimler.sechub.integrationtest.internal.MockEmailAccess;

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
        assertMailExists(to, subject, false);
    }

    /**
     * Assert mail to given test user exists
     * 
     * @param to                  test user
     * @param subject             subject of mail
     * @param asRegularExpression if <code>true</code> then the subject string is
     *                            used as a regular expression.
     */
    public static void assertMailExists(TestUser to, String subject, boolean asRegularExpression) {
        assertMailExists(to.getEmail(), subject, asRegularExpression);
    }

    /**
     * Assert that a mail send to administrator email address exist. An admin email
     * is normally a NPM or a mail distribution address
     * 
     * @param subject             subject of mail
     * @param asRegularExpression if <code>true</code> then the subject string is
     *                            used as a regular expression.
     */
    public static void assertMailToAdminsExists(String subject, boolean asRegularExpression) {
        assertMailExists("int-test_superadmins_npm@example.org", subject, asRegularExpression);
    }

    /**
     * Assert mail to address exists
     * 
     * @param to                  mail address
     * @param subject             subject of mail
     * @param asRegularExpression if <code>true</code> then the subject string is
     *                            used as a regular expression.
     */
    public static void assertMailExists(String to, String subject, boolean asRegularExpression) {
        IntegrationTestContext.get().emailAccess().findMailOrFail(to, subject, asRegularExpression, MockEmailAccess.DEFAULT_TIMEOUT);
    }

}
