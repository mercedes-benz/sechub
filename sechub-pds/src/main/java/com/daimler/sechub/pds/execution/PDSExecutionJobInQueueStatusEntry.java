// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.execution;

import java.time.LocalDateTime;
import java.util.UUID;

import com.daimler.sechub.pds.job.PDSJobStatusState;
import com.daimler.sechub.pds.util.PDSLocalDateTimeDeserializer;
import com.daimler.sechub.pds.util.PDSLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
