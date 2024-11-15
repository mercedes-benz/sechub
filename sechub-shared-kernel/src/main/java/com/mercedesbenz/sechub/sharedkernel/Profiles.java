// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

public class Profiles {

    private Profiles() {
    }

    public static final String LOCALSERVER = "localserver";

    public static final String DEV = "dev";
    public static final String PROD = "prod";
    public static final String TEST = "test";
    /**
     * Special profile suitable for development time - will show extreme debug
     * information. Should never be used in production
     */
    public static final String DEBUG = "debug";
    /**
     * H2 Database profile
     */
    public static final String H2 = "h2";

    /**
     * PostgreSQL Database profile
     */
    public static final String POSTGRES = "postgres";

    /**
     * Security profiles
     */
    public static final String OAUTH2 = "oauth2";

    public static final String MOCKED_NOTIFICATIONS = "mocked_notifications";

    /**
     * This profile enables mocked product adapters and also the possibility to
     * define mock data at project level.
     */
    public static final String MOCKED_PRODUCTS = "mocked_products";

    /**
     * Special profile for integration tests (see project "sechub-integrationtest")
     */
    public static final String INTEGRATIONTEST = "integrationtest";

    /**
     * When an initial administrator must be created the predefined values will be
     * used (So API token is well known) see InitialAdmininInitializer.java
     */
    public static final String INITIAL_ADMIN_PREDEFINED = "initial_admin_predefined";

    /**
     * When an initial administrator must be created a static value will be used (So
     * API token is well known) but this is NOT ENCRYPTED. Use this ONLY for
     * (integration) test purposes! See InitialAdmininInitializer.java
     */
    public static final String INITIAL_ADMIN_STATIC = "initial_admin_static";

    /**
     * The profile ensures an administrator is available inside system on startup.
     * If not an initial administrator account will be created. Information about
     * login and further steps can be found inside server logs. See
     * InitialAdmininInitializer.java
     *
     */
    public static final String INITIAL_ADMIN_CREATED = "initial_admin_created";

    /**
     * Special profile : marked parts are critical and will provide administrative
     * access/ privileges. You should start different servers: 1..n without this
     * profile enabled and listening to standard port. Another server with a
     * dedicated administrative port + a firewall having IP and port filters to
     * administrative users only.
     */
    public static final String ADMIN_ACCESS = "admin_access";
}
