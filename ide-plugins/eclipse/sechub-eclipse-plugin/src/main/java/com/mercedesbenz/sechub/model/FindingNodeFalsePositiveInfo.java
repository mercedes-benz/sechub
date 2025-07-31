package com.mercedesbenz.sechub.model;

import java.util.UUID;

public class FindingNodeFalsePositiveInfo {

	private String comment;
	
	private String author;
	
	private String created;
	
	private Integer findingId;
	
	private UUID jobUUID;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public Integer getFindingId() {
		return findingId;
	}

	public void setFindingId(Integer findingId) {
		this.findingId = findingId;
	}

	public UUID getJobUUID() {
		return jobUUID;
	}

	public void setJobUUID(UUID jobUUID) {
		this.jobUUID = jobUUID;
	}
	
	
}
