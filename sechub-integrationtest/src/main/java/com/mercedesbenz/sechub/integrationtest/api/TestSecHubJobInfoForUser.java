// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationMetaData;
import com.mercedesbenz.sechub.commons.model.TrafficLight;

/**
 * A simple test object to simulate SecHubJobInfoForUser (which is not available
 * from classpath in integration tests)
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSecHubJobInfoForUser {

    public UUID jobUUID;

    public String executedBy;

    public LocalDateTime created;
    public LocalDateTime started;

    public LocalDateTime ended;

    public String executionState;

    public String executionResult;

    public TrafficLight trafficLight;

    public Optional<SecHubConfigurationMetaData> metaData = Optional.empty();

}