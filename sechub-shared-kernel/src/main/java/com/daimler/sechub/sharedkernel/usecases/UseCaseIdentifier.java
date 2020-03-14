// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases;

/**
 * Identifier enumeration for use cases. <br>
 * <br>
 * <b>The ordering of the enums is the ordering used for uniqueId
 * calculation!</b> Calculated ID will be used in documentation to order content.
 * It also is important when comparing different versions of a documentation.<br>
 * For an example:<br>
 * When adding new use cases the
 * <br>
 * So <b>do not change the ordering light hearted!</b> The main purpose why using
 * enums here is to prevent manual managing unique identifiers via string
 * constants. The enum will have always unique ids which can be used for
 * documentation.
 *
 * @author Albert Tregnaghi
 *
 */
public enum UseCaseIdentifier {

	/*
	 * The ordering of the enums is the ordering used for uniqueId calculation! So
	 * do not change the ordering lighthearded!
	 */

	UC_SIGNUP,

	UC_ADMIN_LISTS_OPEN_USER_SIGNUPS,

	UC_ADMIN_ACCEPTS_SIGNUP,

	UC_ADMIN_LISTS_ALL_ACCEPTED_USERS,

	UC_USER_CREATES_JOB,

	UC_USER_UPLOADS_SOURCECODE,

	UC_USER_APPROVES_JOB,

	UC_SCHEDULER_STARTS_JOB,

	UC_USER_GET_JOB_STATUS,

	UC_USER_GET_JOB_REPORT,

	UC_USER_USES_CLIENT_TO_SCAN,

	UC_USER_CLICKS_LINK_TO_GET_NEW_API_TOKEN,

	UC_ADMIN_CREATES_PROJECT,

	UC_ADMIN_LISTS_ALL_PROJECTS,

	UC_ADMIN_ASSIGNS_USER_TO_PROJECT,

	UC_ADMIN_UNASSIGNS_USER_FROM_PROJECT,

	UC_ADMIN_SHOWS_USER_DETAILS,

	UC_ADMIN_DELETES_USER,

	UC_ADMIN_DELETES_SIGNUP,

	UC_ADMIN_DELETES_PROJECT,

	UC_ADMIN_SHOWS_PROJECT_DETAILS,


	UC_UPDATE_PROJECT_WHITELIST,

	UC_ADMIN_LISTS_ALL_RUNNING_JOBS,

	UC_USER_REQUESTS_NEW_APITOKEN,

	UC_USER_SHOWS_PROJECT_SCAN_INFO,

	UC_ADMIN_DOWNLOADS_FULL_DETAILS_ABOUT_SCAN_JOB,

	UC_ADMIN_GRANTS_ADMIN_RIGHT_TO_ANOTHER_USER,

	UC_ADMIN_REVOKES_ADMIN_RIGHTS_FROM_ANOTHER_ADMIN,

	UC_ADMIN_LISTS_ALL_ADMINS,


	UC_ADMIN_DISABLES_SCHEDULER_JOB_PROCESSING,

	UC_ADMIN_ENABLES_SCHEDULER_JOB_PROCESSING,

	UC_ADMIN_TRIGGERS_REFRESH_SCHEDULER_STATUS,

	UC_ADMIN_LIST_STATUS_INFORMATION,

	UC_ADMIN_CANCELS_JOB,
	
	UC_USER_DEFINES_PROJECT_MOCKDATA_CONFIGURATION,
	
	UC_USER_RETRIEVES_PROJECT_MOCKDATA_CONFIGURATION,
	
    UC_ADMIN_UPDATES_MAPPING_CONFIGURATION,
    
    UC_ADMIN_FETCHES_MAPPING_CONFIGURATION,
	
	UC_ANONYMOUS_CHECK_ALIVE,
	
	UC_ADMIN_CHECKS_SERVER_VERSION,
	
	UC_ADMIN_RESTARTS_JOB,
	
	UC_ADMIN_RESTARTS_JOB_HARD,
	
	
	;

	/* +-----------------------------------------------------------------------+ */
	/* +............................ Helpers ................................+ */
	/* +-----------------------------------------------------------------------+ */

	private String uniqueId;

	public String uniqueId() {
		return uniqueId;
	}

	private static final int WANTED_ID_LENGTH = 3;
	private static int counter;

	private UseCaseIdentifier() {
		this.uniqueId = createUseCaseID();
	}

	private static String createUseCaseID() {
		counter++;
		StringBuilder sb = new StringBuilder();

		sb.append(counter);
		while (sb.length() < WANTED_ID_LENGTH) {
			sb.insert(0, "0");
		}

		sb.insert(0, "UC_");
		return sb.toString();
	}
}
