// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds.data;

public enum PDSJobStatusState {

    /**
     * JOB has been created, but not yet marked for ready to start - so e.g. upload
     * of files are missing.
     */
    CREATED,

    /** JOB has been marked to start, but not running */
    READY_TO_START,

    /** JOB is queued */
    QUEUED,

    /** JOB is currently running */
    RUNNING,

    /** JOB cancelation has been requested */
    CANCEL_REQUESTED,

    /** JOB has been canceled */
    CANCELED,

    /** JOB has failed */
    FAILED,

    /** JOB has been done, result available */
    DONE,

    ;

}
