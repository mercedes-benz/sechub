// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.schedule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.commons.model.TrafficLight;
import com.daimler.sechub.domain.schedule.job.ScheduleSecHubJob;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This class represents the schedule job status which can be obtained by REST
 *
 * @author Albert Tregnaghi
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@MustBeKeptStable("This is the job status returned from REST API. Must be kept stable for cli client and other systems")
public class ScheduleJobStatus implements JSONable<ScheduleJobStatus> {

	public static final String PROPERTY_JOBUUID = "jobUUID";
	/**
	 * The "owner" is only the user who started the job. Don't mix up with entity "Owner" who is responsible for the projects!
	 */
	public static final String PROPERTY_OWNER= "owner";
	public static final String PROPERTY_CREATED= "created";
	public static final String PROPERTY_STARTED = "started";
	public static final String PROPERTY_ENDED= "ended";
	public static final String PROPERTY_STATE= "state";
	public static final String PROPERTY_RESULT= "result";
	public static final String PROPERTY_TRAFFICLIGHT= "trafficLight";


	UUID jobUUID;

	String owner;

	String created;
	String started;
	String ended;

	String state;
	String result;

	String trafficLight;

	ScheduleJobStatus() {

	}

	public ScheduleJobStatus(ScheduleSecHubJob secHubJob) {
		this.jobUUID = secHubJob.getUUID();

		/*
		 * why are nearly all parts represented as string and not direct parts? because
		 * I didn't like "null" appearing in output to user - thats all
		 */
		this.owner = secHubJob.getOwner();

		this.created = convertToString(secHubJob.getCreated());
		this.started = convertToString(secHubJob.getStarted());
		this.ended = convertToString(secHubJob.getEnded());

		this.state = convertToString(secHubJob.getExecutionState());
		this.result = convertToString(secHubJob.getExecutionResult());
		this.trafficLight = convertToString(secHubJob.getTrafficLight());
	}

	private String convertToString(ExecutionResult result) {
		if (result == null) {
			return "";
		}
		return result.name();
	}

	private String convertToString(ExecutionState state) {
		if (state == null) {
			return "";
		}
		return state.name();
	}

	private String convertToString(TrafficLight trafficLight) {
		if (trafficLight == null) {
			return "";
		}
		return trafficLight.name();
	}

	private String convertToString(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return "";
		}
		return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	@Override
	public Class<ScheduleJobStatus> getJSONTargetClass() {
		return ScheduleJobStatus.class;
	}

}
