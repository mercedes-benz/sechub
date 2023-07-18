// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mercedesbenz.sechub.commons.model.SecHubLocalDateTimeDeserializer;
import com.mercedesbenz.sechub.commons.model.SecHubLocalDateTimeSerializer;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;

public class PDSExecutionJobInQueueStatusEntry {

    public UUID jobUUID;

    public boolean done;

    public boolean canceled;

    public PDSJobStatusState state;

    @JsonDeserialize(using = SecHubLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SecHubLocalDateTimeSerializer.class)
    public LocalDateTime created;

    @JsonDeserialize(using = SecHubLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SecHubLocalDateTimeSerializer.class)
    public LocalDateTime started;

}
