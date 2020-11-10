package com.daimler.sechub.domain.administration.project;

import java.io.Serializable;

public class ProjectMetaData implements Serializable {

	private static final long serialVersionUID = -721445637539759118L;
	
	private String projectId;
	private String key;
	private String value;
	
	public ProjectMetaData() {
		// jpa only
	}
	
	public ProjectMetaData(String projectId, String key, String value) {
		this.projectId = projectId;
		this.key = key;
		this.value = value;
	}
	
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getProjectId() {
		return projectId;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		ProjectMetaData other = (ProjectMetaData) obj;
		
		return other.projectId.equals(projectId) && other.key.equals(key) && other.value.equals(value);  
	}

}
