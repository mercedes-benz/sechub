// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.administration;

import static com.mercedesbenz.sechub.sharedkernel.security.APIConstants.API_ADMINISTRATION;
import static com.mercedesbenz.sechub.sharedkernel.security.APIConstants.API_ANONYMOUS;

public class AdministrationAPIConstants {

    private AdministrationAPIConstants() {
    }

    /* +-----------------------------------------------------------------------+ */
    /* +............................ SIGN UP ................................+ */
    /* +-----------------------------------------------------------------------+ */
    /**
     * Signup by a new user
     */
    public static final String API_SIGNUP = API_ANONYMOUS + "signup";

    /**
     * Delete signup
     */
    public static final String API_DELETE_SIGNUP = API_ADMINISTRATION + "signup/{userId}";

    /**
     * Accept sign up by admin
     */
    public static final String API_ACCEPT_USER_SIGNUP = API_ADMINISTRATION + "signup/accept/{userId}";

    /**
     * Stop scheduling (pause)
     */
    public static final String API_SCHEDULER_DISABLE_JOB_PROCESSING = API_ADMINISTRATION + "scheduler/disable/job-processing";

    /**
     * Stop scheduling (pause)
     */
    public static final String API_SCHEDULER_ENABLE_JOB_PROCESSING = API_ADMINISTRATION + "scheduler/enable/job-processing";

    /*
     * TODO Albert Tregnaghi, 2020-03-16: next ones are inside admin layer not
     * scheduler! so we should rename these constants!
     */
    /**
     * Refresh scheduler status
     */
    public static final String API_SCHEDULER_STATUS_REFRESH = API_ADMINISTRATION + "scheduler/status/refresh";

    public static final String API_SCHEDULER_GET_STATUS = API_ADMINISTRATION + "status";

    /**
     * Admin only parts
     */
    public static final String API_ADMIN_CANCELS_JOB = API_ADMINISTRATION + "jobs/cancel/{jobUUID}";

    public static final String API_ADMIN_RESTARTS_JOB = API_ADMINISTRATION + "jobs/restart/{jobUUID}";;
    public static final String API_ADMIN_RESTARTS_JOB_HARD = API_ADMINISTRATION + "jobs/restart-hard/{jobUUID}";;

    public static final String API_ADMIN_FETCHES_AUTOCLEAN_CONFIG = API_ADMINISTRATION + "config/autoclean";
    public static final String API_ADMIN_UPDATES_AUTOCLEAN_CONFIG = API_ADMINISTRATION + "config/autoclean";

    /**
     * show all users wanting to sign up
     */
    public static final String API_LIST_USER_SIGNUPS = API_ADMINISTRATION + "signups";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Users ....................................+ */
    /* +-----------------------------------------------------------------------+ */
    /**
     * shows all users names
     */
    public static final String API_LIST_ALL_USERS = API_ADMINISTRATION + "users";

    public static final String API_LIST_ALL_ADMINS = API_ADMINISTRATION + "admins";

    public static final String API_CREATE_PROJECT = API_ADMINISTRATION + "project";

    public static final String API_LIST_ALL_PROJECTS = API_ADMINISTRATION + "projects";
    // inspired by
    // https://developer.github.com/v3/orgs/members/#add-or-update-organization-membership
    private static final String COMMON_API_PROJECT_MEMBERSHIPS = API_ADMINISTRATION + "project/{projectId}/membership/{userId}";

    public static final String API_ASSIGN_OWNER_TO_PROJECT = API_ADMINISTRATION + "project/{projectId}/owner/{userId}";

    public static final String API_ASSIGN_USER_TO_PROJECT = COMMON_API_PROJECT_MEMBERSHIPS;
    public static final String API_UNASSIGN_USER_TO_PROJECT = COMMON_API_PROJECT_MEMBERSHIPS;

    public static final String API_GRANT_ADMIN_RIGHTS_TO_USER = API_ADMINISTRATION + "user/{userId}/grant/superadmin";
    public static final String API_REVOKE_ADMIN_RIGHTS_FROM_USER = API_ADMINISTRATION + "user/{userId}/revoke/superadmin";

    public static final String API_SHOW_USER_DETAILS = API_ADMINISTRATION + "user/{userId}";
    public static final String API_SHOW_USER_BY_EMAIL_DETAILS = API_ADMINISTRATION + "user-by-email/{emailAddress}";

    public static final String API_DELETE_USER = API_ADMINISTRATION + "user/{userId}";
    public static final String API_UPDATE_USER_EMAIL_ADDRESS = API_ADMINISTRATION + "user/{userId}/email/{newEmailAddress}";

    public static final String API_SHOW_PROJECT_DETAILS = API_ADMINISTRATION + "project/{projectId}";
    public static final String API_CHANGE_PROJECT_DETAILS = API_ADMINISTRATION + "project/{projectId}";
    public static final String API_DELETE_PROJECT = API_ADMINISTRATION + "project/{projectId}";
    public static final String API_UPDATE_PROJECT_WHITELIST = API_ADMINISTRATION + "project/{projectId}/whitelist";
    public static final String API_UPDATE_PROJECT_METADATA = API_ADMINISTRATION + "project/{projectId}/metadata";

    public static final String API_LIST_JOBS_RUNNING = API_ADMINISTRATION + "jobs/running";

    public static final String API_CONFIG_MAPPING = API_ADMINISTRATION + "config/mapping/{mappingId}";

    public static final String API_CHANGE_PROJECT_ACCESSLEVEL = API_ADMINISTRATION + "project/{projectId}/accesslevel/{projectAccessLevel}";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Encryption................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String API_ADMIN_ENCRYPTION_ROTATION = API_ADMINISTRATION + "encryption/rotate";
    public static final String API_ADMIN_ENCRYPTION_STATUS = API_ADMINISTRATION + "encryption/status";

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Anonymous ................................+ */
    /* +-----------------------------------------------------------------------+ */
    public static final String API_FETCH_NEW_API_TOKEN_BY_ONE_WAY_TOKEN = API_ANONYMOUS + "apitoken";
    public static final String API_REQUEST_NEW_APITOKEN = API_ANONYMOUS + "refresh/apitoken/{emailAddress}";

}
