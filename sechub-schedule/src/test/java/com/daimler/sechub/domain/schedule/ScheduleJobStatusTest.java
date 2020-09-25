// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;

public class ScheduleJobStatusTest {

	private static final String LOCALDATETIME_1_AS_STRING = "2018-03-02T15:43:15.304";
	private ScheduleSecHubJob secHubJob;
	private ScheduleJobStatus statusToTest;
	private LocalDateTime localDateTime1 = LocalDateTime.parse(LOCALDATETIME_1_AS_STRING);

	@Before
	public void before() throws Exception {
		secHubJob = mock(ScheduleSecHubJob.class);
	}

	@Test
	public void when_traficclight_is_null_status_has_empty_string() {
		/* execute */
		statusToTest = new ScheduleJobStatus(secHubJob);

		/* test */
		assertEquals("", statusToTest.trafficLight);
	}

	@Test
	public void when_created_is_null_status_has_empty_string() {
		/* execute */
		statusToTest = new ScheduleJobStatus(secHubJob);

		/* test */
		assertEquals("", statusToTest.created);
	}

	@Test
	public void when_ended_is_null_status_has_empty_string() {
		/* execute */
		statusToTest = new ScheduleJobStatus(secHubJob);

		/* test */
		assertEquals("", statusToTest.ended);
	}

	@Test
	public void when_started_is_null_status_has_empty_string() {
		/* execute */
		statusToTest = new ScheduleJobStatus(secHubJob);

		/* test */
		assertEquals("", statusToTest.started);
	}

	@Test
	public void when_trafficlight_is_GREEN_status_has_GREEN() {
		/* prepare */

		when(secHubJob.getTrafficLight()).thenReturn(TrafficLight.GREEN);

		/* execute */
		statusToTest = new ScheduleJobStatus(secHubJob);

		/* test */
		assertEquals("GREEN", statusToTest.trafficLight);
	}

	@Test
	public void when_trafficlight_is_RED_status_has_RED() {
		/* prepare */

		when(secHubJob.getTrafficLight()).thenReturn(TrafficLight.RED);

		/* execute */
		statusToTest = new ScheduleJobStatus(secHubJob);

		/* test */
		assertEquals("RED", statusToTest.trafficLight);
	}

	@Test
	public void when_trafficlight_is_YELLOW_status_has_YELLOW() {
		/* prepare */

		when(secHubJob.getTrafficLight()).thenReturn(TrafficLight.YELLOW);

		/* execute */
		statusToTest = new ScheduleJobStatus(secHubJob);

		/* test */
		assertEquals("YELLOW", statusToTest.trafficLight);
	}

	@Test
	public void when_created_is_NOT_null_status_has_localdate_time_string() {
		/* prepare */

		when(secHubJob.getCreated()).thenReturn(localDateTime1);

		/* execute */
		statusToTest = new ScheduleJobStatus(secHubJob);

		/* test */
		assertEquals(LOCALDATETIME_1_AS_STRING, statusToTest.created);
	}

	@Test
	public void when_started_is_NOT_null_status_has_localdate_time_string() {
		/* prepare */

		when(secHubJob.getStarted()).thenReturn(localDateTime1);

		/* execute */
		statusToTest = new ScheduleJobStatus(secHubJob);

		/* test */
		assertEquals(LOCALDATETIME_1_AS_STRING, statusToTest.started);
	}

	@Test
	public void when_ended_is_NOT_null_status_has_localdate_time_string() {
		/* prepare */

		when(secHubJob.getEnded()).thenReturn(localDateTime1);

		/* execute */
		statusToTest = new ScheduleJobStatus(secHubJob);

		/* test */
		assertEquals(LOCALDATETIME_1_AS_STRING, statusToTest.ended);
	}

}
