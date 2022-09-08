// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.pds.data;

import java.util.UUID;

public class PDSJobStatus {

    public UUID jobUUID;

    public String owner;

    public String created;
    public String started;
    public String ended;

    public PDSJobStatusState state;

    @Override
    public String toString() {
        return "PDSJobStatus [" + (state != null ? "state=" + state + ", " : "") + (jobUUID != null ? "jobUUID=" + jobUUID + ", " : "")
                + (owner != null ? "owner=" + owner + ", " : "") + (created != null ? "created=" + created + ", " : "")
                + (started != null ? "started=" + started + ", " : "") + (ended != null ? "ended=" + ended : "") + "]";
    }

}
