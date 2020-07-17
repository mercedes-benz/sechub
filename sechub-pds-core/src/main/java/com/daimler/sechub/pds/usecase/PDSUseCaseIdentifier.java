package com.daimler.sechub.pds.usecase;

public enum PDSUseCaseIdentifier {

    UC_USER_CREATES_JOB,
    
    UC_USER_UPLOADS_JOB_DATA,
    
    UC_USER_MARKS_JOB_READY_TO_START,
    
    UC_USER_CANCELS_JOB,
    
    UC_USER_FETCHES_STATUS_OF_JOB,
    
    UC_USER_FETCHES_JOB_RESULT,
    
    UC_ADMIN_FETCHES_MONITORING_STATUS,
    
    UC_ANONYMOUS_CHECK_ALIVE,
    
    UC_ADMIN_FETCHES_JOB_RESULT_OR_FAILURE_TEXT,
    
    UC_ADMIN_FETCHES_SERVER_CONFIGURATION,
    ;
    
    /* +---------------------------------------------------------------------+ */
    /* +............................ Helpers ................................+ */
    /* +---------------------------------------------------------------------+ */

    private String uniqueId;

    public String uniqueId() {
        return uniqueId;
    }

    private static final int WANTED_ID_LENGTH = 3;
    private static int counter;

    private PDSUseCaseIdentifier() {
        this.uniqueId = createUseCaseID();
    }

    private static String createUseCaseID() {
        counter++;
        StringBuilder sb = new StringBuilder();

        sb.append(counter);
        while (sb.length() < WANTED_ID_LENGTH) {
            sb.insert(0, "0");
        }

        sb.insert(0, "PDS_UC_");
        return sb.toString();
    }
    
}
