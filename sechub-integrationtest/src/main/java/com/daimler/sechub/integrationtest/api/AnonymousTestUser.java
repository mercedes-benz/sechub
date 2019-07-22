// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

/**
 * Those test users are not managed and so not wellknown - use them for temporary objects for action which should not be possible!
 * There is no auto cleanup for those parts - so they MAY NOTbe persisted at all...
 * @author Albert Tregnaghi
 *
 */
public class AnonymousTestUser extends TestUser{

	public AnonymousTestUser(String userid, String email) {
		super(userid,"",email);
	}
	
}
