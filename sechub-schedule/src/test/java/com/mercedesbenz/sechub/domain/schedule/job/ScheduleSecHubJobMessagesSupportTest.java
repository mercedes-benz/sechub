// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.job;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

class ScheduleSecHubJobMessagesSupportTest {

    private ScheduleSecHubJobMessagesSupport supportToTest;

    @BeforeEach
    void before() {
        supportToTest = new ScheduleSecHubJobMessagesSupport();
    }

    @Test
    void fetchMessagesOrNull_null_returns_null() {
        assertNull(supportToTest.fetchMessagesOrNull(null));
    }

    @Test
    void fetchMessagesOrNull_job_without_messages_returns_null() {
        /* prepare */
        ScheduleSecHubJob job = new ScheduleSecHubJob();

        /* check precondition */
        assertNull(job.getJsonMessages());

        /* execute + test */
        assertNull(supportToTest.fetchMessagesOrNull(job));
    }

    @Test
    void addMessages_does_set_job_json_messages_as_expected() {
        /* prepare */
        ScheduleSecHubJob job = new ScheduleSecHubJob();

        /* check precondition */
        assertNull(job.getJsonMessages());

        /* test */
        supportToTest.addMessages(job,
                Arrays.asList(new SecHubMessage(SecHubMessageType.ERROR, "an error"), new SecHubMessage(SecHubMessageType.INFO, "an info")));

        /* test */
        assertNotNull(job.getJsonMessages());
        assertEquals("[{\"type\":\"ERROR\",\"text\":\"an error\"},{\"type\":\"INFO\",\"text\":\"an info\"}]", job.getJsonMessages());
    }

    @Test
    void add_and_fetchMessagesOrNull_job_with_messages_returns_added_parts() {
        /* prepare */
        ScheduleSecHubJob job = new ScheduleSecHubJob();

        /* check precondition */
        assertNull(job.getJsonMessages());

        /* execute + test */
        assertNull(supportToTest.fetchMessagesOrNull(job));
    }

}
