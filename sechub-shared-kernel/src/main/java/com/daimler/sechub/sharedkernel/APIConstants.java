// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel;

import com.daimler.sechub.sharedkernel.configuration.AbstractAllowSecHubAPISecurityConfiguration;

/**
 * API constants, usable inside rest controllers etc. Be AWARE: its very
 * important to start the api constants always with an "/" e.g. "/api/admin/"
 * because otherwise spring boot will NOT setup security correct.<br>
 * <br>
 * To prevent wrong configuration the
 * {@link AbstractAllowSecHubAPISecurityConfiguration} has now a an denyAll
 * block to prevent anonymous access because of configuration failures.
 *
 * @author Albert Tregnaghi
 *
 */
public class APIConstants {

	private APIConstants() {

	}

	/**
	 * API starting with this all is accessible - even anonymous!
	 */
	public static final String API_ANONYMOUS = "/api/anonymous/";

	/**
	 * API starting with this only admins can access!
	 */
	public static final String API_ADMINISTRATION = "/api/admin/";

	/**
	 * API starting with this is accessible by users for their profile
	 */
	public static final String API_USER = "/api/user/";

	/**
	 * API starting with this is accessible by owners for their profile
	 */
	public static final String API_OWNER = "/api/owner/";

	/**
	 * API starting with this is accessible by users for their projects
	 */
	public static final String API_PROJECT = "/api/project/";
	
	/**
	 * Actuator endpoints are available anonymous
	 */
	public static final String ACTUATOR = "/actuator/";

}
