// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

/**
 * Those test users are not managed and so not wellknown - use them for
 * temporary objects for action which should not be possible! There is no auto
 * cleanup for those parts - so they MAY NOTbe persisted at all...
 *
 * @author Albert Tregnaghi
 *
 */
public class AnonymousTestUser extends FixedTestUser {

    public AnonymousTestUser(String userid) {
        super(userid, "", null);
    }

    public AnonymousTestUser(String userid, String specialMail) {
        super(userid, "", specialMail);
    }

}
