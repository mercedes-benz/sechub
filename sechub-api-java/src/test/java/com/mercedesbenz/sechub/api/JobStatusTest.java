// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.api.internal.gen.model.OpenApiJobStatus;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.job.ExecutionResult;
import com.mercedesbenz.sechub.commons.model.job.ExecutionState;

class JobStatusTest {

    @Test
    void from_status_results_in_correct_object() {
        /* prepare */
        LocalDateTime created = LocalDateTime.now().minus(3, ChronoUnit.MINUTES);
        LocalDateTime started = LocalDateTime.now().minus(2, ChronoUnit.MINUTES);
        LocalDateTime ended = LocalDateTime.now().minus(1, ChronoUnit.MINUTES);
        TrafficLight trafficLight = TrafficLight.GREEN;
        String owner = "owner1";
        UUID jobUUID = UUID.randomUUID();
        ExecutionResult executionResult = ExecutionResult.OK;
        ExecutionState state = ExecutionState.ENDED;

        OpenApiJobStatus status = mock(OpenApiJobStatus.class);
        when(status.getTrafficLight()).thenReturn(trafficLight.name());

        when(status.getCreated()).thenReturn(created.toString());
        when(status.getStarted()).thenReturn(started.toString());
        when(status.getEnded()).thenReturn(ended.toString());
        when(status.getOwner()).thenReturn(owner);
        when(status.getJobUUID()).thenReturn(jobUUID.toString());
        when(status.getState()).thenReturn(state.toString());
        when(status.getResult()).thenReturn(executionResult.name());

        /* execute */
        JobStatus result = JobStatus.from(status);

        /* test */
        assertEquals(created, result.getCreated());
        assertEquals(started, result.getStarted());
        assertEquals(ended, result.getEnded());
        assertEquals(trafficLight, result.getTrafficLight());
        assertEquals(owner, result.getOwner());
        assertEquals(jobUUID, result.getJobUUID());
        assertEquals(state, result.getState());
        assertEquals(executionResult, result.getResult());
    }

}
