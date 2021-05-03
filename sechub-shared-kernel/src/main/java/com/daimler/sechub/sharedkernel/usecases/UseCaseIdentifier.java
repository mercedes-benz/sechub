// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.usecases;

/**
 * Identifier enumeration for use cases. <br>
 * <br>
 * The ordering of the enums does not matter because the usecase number is given 
 * at construction time and used for unique id generation.<br><br>But DO NOT change the numbers
 * because they are used inside asciidoc documentation files for references and would not be 
 * valid any longer when there are changes made.
 * 
 * @author Albert Tregnaghi
 *
 */
public enum UseCaseIdentifier {

    UC_SIGNUP(1),

    UC_ADMIN_LISTS_OPEN_USER_SIGNUPS(2),

    UC_ADMIN_ACCEPTS_SIGNUP(3),

    UC_ADMIN_LISTS_ALL_ACCEPTED_USERS(4),

    UC_USER_CREATES_JOB(5),

    UC_USER_UPLOADS_SOURCECODE(6),

    UC_USER_APPROVES_JOB(7),

    UC_SCHEDULER_STARTS_JOB(8),

    UC_USER_GET_JOB_STATUS(9),

    UC_USER_GET_JOB_REPORT(10),

    UC_USER_USES_CLIENT_TO_SCAN(11),

    UC_USER_CLICKS_LINK_TO_GET_NEW_API_TOKEN(12),

    UC_ADMIN_CREATES_PROJECT(13),

    UC_ADMIN_LISTS_ALL_PROJECTS(14),

    UC_ADMIN_ASSIGNS_USER_TO_PROJECT(15),

    UC_ADMIN_UNASSIGNS_USER_FROM_PROJECT(16),

    UC_ADMIN_SHOWS_USER_DETAILS(17),

    UC_ADMIN_DELETES_USER(18),

    UC_ADMIN_DELETES_SIGNUP(19),

    UC_ADMIN_DELETES_PROJECT(20),

    UC_ADMIN_SHOWS_PROJECT_DETAILS(21),

    UC_UPDATE_PROJECT_WHITELIST(22),

    UC_ADMIN_LISTS_ALL_RUNNING_JOBS(23),

    UC_USER_REQUESTS_NEW_APITOKEN(24),

    UC_USER_SHOWS_PROJECT_SCAN_INFO(25),

    UC_ADMIN_DOWNLOADS_FULL_DETAILS_ABOUT_SCAN_JOB(26),

    UC_ADMIN_GRANTS_ADMIN_RIGHT_TO_ANOTHER_USER(27),

    UC_ADMIN_REVOKES_ADMIN_RIGHTS_FROM_ANOTHER_ADMIN(28),

    UC_ADMIN_LISTS_ALL_ADMINS(29),

    UC_ADMIN_DISABLES_SCHEDULER_JOB_PROCESSING(30),

    UC_ADMIN_ENABLES_SCHEDULER_JOB_PROCESSING(31),

    UC_ADMIN_TRIGGERS_REFRESH_SCHEDULER_STATUS(32),

    UC_ADMIN_LIST_STATUS_INFORMATION(33),

    UC_ADMIN_CANCELS_JOB(34),

    UC_USER_DEFINES_PROJECT_MOCKDATA_CONFIGURATION(35),

    UC_USER_RETRIEVES_PROJECT_MOCKDATA_CONFIGURATION(36),

    UC_ADMIN_UPDATES_MAPPING_CONFIGURATION(37),

    UC_ADMIN_FETCHES_MAPPING_CONFIGURATION(38),

    UC_ANONYMOUS_CHECK_ALIVE(39),

    UC_ADMIN_CHECKS_SERVER_VERSION(40),

    UC_ADMIN_RESTARTS_JOB(41),

    UC_ADMIN_RESTARTS_JOB_HARD(42),

    UC_ADMIN_RECEIVES_NOTIFICATOIN_ABOUT_CLUSTER_MEMBER_START(43),

    UC_USER_MARKS_FALSE_POSITIVES_FOR_FINISHED_JOB(44),

    UC_USER_UNMARKS_FALSE_POSITIVES(45),

    UC_USER_FETCHES_FALSE_POSITIVE_CONFIGURATION_OF_PROJECT(46),

    /* executors */
    UC_ADMIN_CREATES_EXECUTOR_CONFIGURATION(47),

    UC_ADMIN_DELETES_EXECUTOR_CONFIGURATION(48),

    UC_ADMIN_FETCHES_EXECUTOR_CONFIGURATION_LIST(49),

    UC_ADMIN_FETCHES_EXECUTOR_CONFIGURATION(50),

    UC_ADMIN_UPDATES_EXECUTOR_CONFIGURATION(51),

    /* execution profiles */
    UC_ADMIN_CREATES_EXECUTION_PROFILE(52),

    UC_ADMIN_DELETES_EXECUTION_PROFILE(53),

    UC_ADMIN_UPDATES_EXECUTION_PROFILE(54),

    UC_ADMIN_FETCHES_EXECUTION_PROFILE(55),

    UC_ADMIN_FETCHES_EXECUTION_PROFILE_LIST(56),

    UC_ADMIN_ASSIGNS_EXECUTION_PROFILE_TO_PROJECT(57),

    UC_ADMIN_UNASSIGNS_EXECUTION_PROFILE_FROM_PROJECT(58),

    /* project administration */
    UC_ADMIN_UPDATES_PROJECT_METADATA(59),
    UC_ADMIN_CHANGES_PROJECT_OWNER(60),
    
    UC_ADMIN_CHANGES_PROJECT_DESCRIPTION(61),
    ;

    /* +-----------------------------------------------------------------------+ */
    /* +............................ Helpers ................................+ */
    /* +-----------------------------------------------------------------------+ */

    private String uniqueId;

    public String uniqueId() {
        return uniqueId;
    }

    private static final int WANTED_ID_LENGTH = 3;
    
    private UseCaseIdentifier(int number) {
        this.uniqueId = createUseCaseID(number);
    }

    static String createUseCaseID(int usecaseNumber) {
        StringBuilder sb = new StringBuilder();

        sb.append(usecaseNumber);
        while (sb.length() < WANTED_ID_LENGTH) {
            sb.insert(0, "0");
        }

        sb.insert(0, "UC_");
        return sb.toString();
    }
}
