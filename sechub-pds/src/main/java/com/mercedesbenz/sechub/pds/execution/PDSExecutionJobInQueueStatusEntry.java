// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mercedesbenz.sechub.commons.model.LocalDateTimeDeserializer;
import com.mercedesbenz.sechub.commons.model.LocalDateTimeSerializer;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;

public class PDSExecutionJobInQueueStatusEntry {

    public UUID jobUUID;

    public boolean done;

    public boolean canceled;

    public PDSJobStatusState state;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime created;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime started;

}
