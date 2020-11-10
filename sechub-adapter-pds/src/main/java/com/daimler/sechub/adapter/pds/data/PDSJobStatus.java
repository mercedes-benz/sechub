// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.pds.data;

import java.util.UUID;

public class PDSJobStatus {

    /**
     * Pendant to PDSJobStatusState.java (necessary because PDS is a standalone
     * application and we do not have any dependencies here.
     */
    public enum PDSAdapterJobStatusState {

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

    public UUID jobUUID;

    public String owner;

    public String created;
    public String started;
    public String ended;

    public PDSAdapterJobStatusState state;

    @Override
    public String toString() {
        return "PDSJobStatus [" + (state != null ? "state=" + state + ", " : "") + (jobUUID != null ? "jobUUID=" + jobUUID + ", " : "")
                + (owner != null ? "owner=" + owner + ", " : "") + (created != null ? "created=" + created + ", " : "")
                + (started != null ? "started=" + started + ", " : "") + (ended != null ? "ended=" + ended : "") + "]";
    }

}
