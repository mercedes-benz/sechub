// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.domain.schedule.job.ScheduleSecHubJob;

public class ScheduleJobStatusTest {

    private static final String LOCALDATETIME_1_AS_STRING = "2018-03-02T15:43:15.304";
    private ScheduleSecHubJob secHubJob;
    private ScheduleJobStatus statusToTest;
    private LocalDateTime localDateTime1 = LocalDateTime.parse(LOCALDATETIME_1_AS_STRING);

    @BeforeEach
    void before() throws Exception {
        secHubJob = mock(ScheduleSecHubJob.class);
    }

    @Test
    void messages_null_json_output_does_not_contain_any_message_element() {
        /* prepare */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* check precondition */
        assertNull(statusToTest.messages);

        /* execute */
        String json = statusToTest.toJSON();

        /* test */
        assertEquals("{\"created\":\"\",\"started\":\"\",\"ended\":\"\",\"state\":\"\",\"result\":\"\",\"trafficLight\":\"\"}", json);
    }

    @Test
    void messages_has_list_with_two_entries_json_output_contains_messages_element_with_two_message_objects() {
        /* prepare */
        statusToTest = new ScheduleJobStatus(secHubJob);

        List<SecHubMessage> messages = new ArrayList<>();
        messages.add(new SecHubMessage(SecHubMessageType.ERROR, "i am an error"));
        messages.add(new SecHubMessage(SecHubMessageType.WARNING, "i am a warning"));

        statusToTest.messages = messages; // normally messages shall not be directly set. This is just to test without
                                          // JSON conversion by constructor.

        /* execute */
        String json = statusToTest.toJSON();

        /* test */
        assertEquals(
                "{\"created\":\"\",\"started\":\"\",\"ended\":\"\",\"state\":\"\",\"result\":\"\",\"trafficLight\":\"\",\"messages\":[{\"type\":\"ERROR\",\"text\":\"i am an error\"},{\"type\":\"WARNING\",\"text\":\"i am a warning\"}]}",
                json);
    }

    @Test
    void messages_from_job_are_fetched_and_message_object_created_when_formerly_null() {
        /* prepare */
        List<SecHubMessage> messages = new ArrayList<>();
        messages.add(new SecHubMessage(SecHubMessageType.ERROR, "i am an error"));
        messages.add(new SecHubMessage(SecHubMessageType.WARNING, "i am a warning"));
        when(secHubJob.getJsonMessages()).thenReturn("[{\"type\":\"ERROR\",\"text\":\"i am an error\"},{\"type\":\"WARNING\",\"text\":\"i am a warning\"}]");

        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertNotNull(statusToTest.messages);
        assertEquals(2, statusToTest.messages.size());
        Iterator<SecHubMessage> it = statusToTest.messages.iterator();
        SecHubMessage obj1 = it.next();
        assertEquals(SecHubMessageType.ERROR, obj1.getType());
        assertEquals("i am an error", obj1.getText());

        SecHubMessage obj2 = it.next();
        assertEquals(SecHubMessageType.WARNING, obj2.getType());
        assertEquals("i am a warning", obj2.getText());
    }

    @Test
    void when_traficclight_is_null_status_has_empty_string() {
        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertEquals("", statusToTest.trafficLight);
    }

    @Test
    void when_created_is_null_status_has_empty_string() {
        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertEquals("", statusToTest.created);
    }

    @Test
    void when_ended_is_null_status_has_empty_string() {
        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertEquals("", statusToTest.ended);
    }

    @Test
    void when_started_is_null_status_has_empty_string() {
        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertEquals("", statusToTest.started);
    }

    @Test
    void when_trafficlight_is_GREEN_status_has_GREEN() {
        /* prepare */

        when(secHubJob.getTrafficLight()).thenReturn(TrafficLight.GREEN);

        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertEquals("GREEN", statusToTest.trafficLight);
    }

    @Test
    void when_trafficlight_is_RED_status_has_RED() {
        /* prepare */

        when(secHubJob.getTrafficLight()).thenReturn(TrafficLight.RED);

        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertEquals("RED", statusToTest.trafficLight);
    }

    @Test
    void when_trafficlight_is_YELLOW_status_has_YELLOW() {
        /* prepare */

        when(secHubJob.getTrafficLight()).thenReturn(TrafficLight.YELLOW);

        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertEquals("YELLOW", statusToTest.trafficLight);
    }

    @Test
    void when_created_is_NOT_null_status_has_localdate_time_string() {
        /* prepare */

        when(secHubJob.getCreated()).thenReturn(localDateTime1);

        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertEquals(LOCALDATETIME_1_AS_STRING, statusToTest.created);
    }

    @Test
    void when_started_is_NOT_null_status_has_localdate_time_string() {
        /* prepare */

        when(secHubJob.getStarted()).thenReturn(localDateTime1);

        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertEquals(LOCALDATETIME_1_AS_STRING, statusToTest.started);
    }

    @Test
    void when_ended_is_NOT_null_status_has_localdate_time_string() {
        /* prepare */

        when(secHubJob.getEnded()).thenReturn(localDateTime1);

        /* execute */
        statusToTest = new ScheduleJobStatus(secHubJob);

        /* test */
        assertEquals(LOCALDATETIME_1_AS_STRING, statusToTest.ended);
    }

}
