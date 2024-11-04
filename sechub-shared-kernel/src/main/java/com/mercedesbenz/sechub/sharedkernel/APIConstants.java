// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel;

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
     * Just a pseudo location string, which is used when no API is available. This
     * can be useful inside usecases where we have no API interfaces available.
     */
    public static final String NO_API_AVAILABLE = "/no/api/available";

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
     * get all assigned / owned projects and details
     */
    public static final String API_PROJECTS = "/api/projects";

    /**
     * Actuator endpoints are available anonymous
     */
    public static final String ACTUATOR = "/actuator/";

    /**
     * Error page
     */
    public static final String ERROR_PAGE = "/error";

}
