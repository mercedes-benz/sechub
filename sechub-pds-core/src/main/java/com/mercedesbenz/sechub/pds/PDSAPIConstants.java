// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds;

public class PDSAPIConstants {

    public static final String API_JOB = "/api/job/";
    public static final String API_ADMIN = "/api/admin/";
    public static final String API_ANONYMOUS = "/api/anonymous/";

    /**
     * Error page
     */
    public static final String ERROR_PAGE = "/error";

    /* auto cleanup */
    public static final String API_AUTOCLEAN = API_ADMIN + "config/autoclean";
    public static final String API_SERVER_CONFIG = API_ADMIN + "config/server";
}
