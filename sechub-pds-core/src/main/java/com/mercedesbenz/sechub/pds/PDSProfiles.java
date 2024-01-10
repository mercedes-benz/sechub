// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

public class PDSProfiles {
    private PDSProfiles() {
    }

    public static final String LOCALSERVER = "pds_localserver";

    public static final String DEV = "pds_dev";
    public static final String PROD = "pds_prod";
    public static final String SQL_TRACE = "pds_sqltrace";
    public static final String TEST = "pds_test";
    /**
     * Special profile suitable for development time - will show extreme debug
     * information. Should never be used in production
     */
    public static final String DEBUG = "pds_debug";
    /**
     * H2 Database profile
     */
    public static final String H2 = "pds_h2";

    /**
     * PostgreSQL Database profile
     */
    public static final String POSTGRES = "pds_postgres";

    /**
     * Special profile for integration tests (see project "sechub-integrationtest")
     */
    public static final String INTEGRATIONTEST = "pds_integrationtest";

}
