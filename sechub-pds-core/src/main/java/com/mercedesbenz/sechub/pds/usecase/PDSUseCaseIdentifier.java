// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.usecase;

public enum PDSUseCaseIdentifier {

    /* job */
    UC_USER_CREATES_JOB(1),

    UC_USER_UPLOADS_JOB_DATA(2),

    UC_USER_MARKS_JOB_READY_TO_START(3),

    UC_USER_REQUESTS_JOB_CANCELLATION(4),

    UC_USER_FETCHES_STATUS_OF_JOB(5),

    UC_USER_FETCHES_JOB_RESULT(6),

    UC_USER_FETCHES_JOB_MESSAGES(16),

    UC_ADMIN_FETCHES_JOB_META_DATA(17),

    UC_SYSTEM_HANDLES_JOB_CANCEL_REQUESTS(18, false),

    /* monitoring */
    UC_ADMIN_FETCHES_MONITORING_STATUS(7),

    UC_ANONYMOUS_CHECK_ALIVE(8),

    /* result */
    UC_ADMIN_FETCHES_JOB_RESULT_OR_FAILURE_TEXT(9),

    /* configuration */
    UC_ADMIN_FETCHES_SERVER_CONFIGURATION(10),

    /* streams */
    UC_ADMIN_FETCHES_OUTPUT_STREAM(11),

    UC_ADMIN_FETCHES_ERROR_STREAM(12),

    /* auto cleanup */
    UC_ADMIN_FETCHES_AUTO_CLEANUP_CONFIGURATION(13),

    UC_ADMIN_UPDATES_AUTO_CLEANUP_CONFIGURATION(14),

    UC_SYSTEM_AUTO_CLEANUP_EXECUTION(15, false),
    
    UC_SYSTEM_SIGTERM_HANDLING(16, false),

    ;

    /* +---------------------------------------------------------------------+ */
    /* +............................ Helpers ................................+ */
    /* +---------------------------------------------------------------------+ */

    private String uniqueId;
    private boolean hasRestApi;

    public String uniqueId() {
        return uniqueId;
    }

    private static final int WANTED_ID_LENGTH = 3;

    private PDSUseCaseIdentifier(int usecaseNumber) {
        this(usecaseNumber, true);
    }

    private PDSUseCaseIdentifier(int usecaseNumber, boolean hasRestAPI) {
        this.uniqueId = createUseCaseID(usecaseNumber);
        this.hasRestApi = hasRestAPI;
    }

    public boolean hasRestApi() {
        return hasRestApi;
    }

    static String createUseCaseID(int usecaseNumber) {
        StringBuilder sb = new StringBuilder();

        sb.append(usecaseNumber);
        while (sb.length() < WANTED_ID_LENGTH) {
            sb.insert(0, "0");
        }

        sb.insert(0, "PDS_UC_");
        return sb.toString();
    }

}
