// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mercedesbenz.sechub.pds.job.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.util.PDSLocalDateTimeDeserializer;
import com.mercedesbenz.sechub.pds.util.PDSLocalDateTimeSerializer;

public class PDSExecutionJobInQueueStatusEntry {

    public UUID jobUUID;

    public boolean done;

    public boolean canceled;

    public PDSJobStatusState state;

    @JsonDeserialize(using = PDSLocalDateTimeDeserializer.class)
    @JsonSerialize(using = PDSLocalDateTimeSerializer.class)
    public LocalDateTime created;

    @JsonDeserialize(using = PDSLocalDateTimeDeserializer.class)
    @JsonSerialize(using = PDSLocalDateTimeSerializer.class)
    public LocalDateTime started;

}
