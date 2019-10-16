// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel;

public class Profiles {

	private Profiles() {
	}

	public static final String LOCALSERVER="localserver";

	public static final String DEV="dev";
	public static final String PROD="prod";
	public static final String TEST="test";
	/**
	 * H2 Database profile
	 */
	public static final String H2="h2";

	/**
	 * PostgreSQL Database profile
	 */
	public static final String POSTGRES="postgres";

	public static final String MOCKED_NOTIFICATIONS="mocked_notifications";

	/**
	 * Special profile for integration tests (see project "sechub-integrationtest")
	 */
	public static final String INTEGRATIONTEST="integrationtest";


	/**
	 * Special profile : marked parts are critical and will provide administrative
	 *  access/ priviledges. You should start different servers: 1..n without
	 *  this profile enabled and listening to standard port. Another server with
	 *  a dedicated administrative port + a firewall having IP and port filters
	 *  to administrative users only.
	 */
	public static final String ADMIN_ACCESS="admin_access";
}
