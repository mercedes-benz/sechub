// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * This message data object contains all possible information about a project
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between (api) schedule domain and administration - and maybe others")
public class JobMessage implements JSONable<JobMessage> {

	private UUID jobUUID;

	private String projectId;

	private String owner;

	private String info;

	private String configuration;

	@JsonFormat(pattern = ("yyyy/MM/dd HH:mm:ss"))
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime since;

	private String ownerEmailAddress;

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public void setSince(LocalDateTime date) {
		this.since = date;
	}

	public LocalDateTime getSince() {
		return since;
	}

	@Override
	public Class<JobMessage> getJSONTargetClass() {
		return JobMessage.class;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setJobUUID(UUID jobUUID) {
		this.jobUUID = jobUUID;
	}

	public UUID getJobUUID() {
		return jobUUID;
	}

	public void setOwnerEmailAddress(String emailAdress) {
		this.ownerEmailAddress = emailAdress;
	}

	public String getOwnerEmailAddress() {
		return ownerEmailAddress;
	}

}
