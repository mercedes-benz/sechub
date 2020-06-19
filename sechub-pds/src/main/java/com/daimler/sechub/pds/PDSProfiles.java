package com.daimler.sechub.pds;

public class PDSProfiles {
    private PDSProfiles() {
    }

    public static final String LOCALSERVER="pds_localserver";

    public static final String DEV="pds_dev";
    public static final String PROD="pds_prod";
    public static final String TEST="test";
    /**
     * Special profile suitable for development time - will show extreme debug information.
     * Should never be used in production 
     */
    public static final String DEBUG="pds_debug";
    /**
     * H2 Database profile
     */
    public static final String H2="pds_h2";

    /**
     * PostgreSQL Database profile
     */
    public static final String POSTGRES="pds_postgres";

    /**
     * Special profile for integration tests (see project "sechub-integrationtest")
     */
    public static final String INTEGRATIONTEST="pds_integrationtest";

    /**
     * When an initial administrator must be created the predefined values will be used
     * (So API token is well known) see InitialAdmininInitializer.java
     */
    public static final String INITIAL_ADMIN_PREDEFINED="pds_initial_admin_predefined";
    
    /**
     * When an initial administrator must be created a static value will be used
     * (So API token is well known) but this is NOT ENCRYPTED. Use this ONLY for (integration) test purposes!
     * See InitialAdmininInitializer.java
     */
    public static final String INITIAL_ADMIN_STATIC="pds_initial_admin_static";
    
    /**
     * The profile ensures an administrator is available inside system on startup.
     * If not an initial administrator account will be created. Information about 
     * login and further steps can be found inside server logs.
     * See InitialAdmininInitializer.java
     * 
     */
    public static final String INITIAL_ADMIN_CREATED="pds_initial_admin_created";
    
}
