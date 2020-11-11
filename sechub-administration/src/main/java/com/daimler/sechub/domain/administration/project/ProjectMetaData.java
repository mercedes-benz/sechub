package com.daimler.sechub.domain.administration.project;

import java.io.Serializable;

public class ProjectMetaData implements Serializable {

	private static final long serialVersionUID = -721445637539759118L;
	
	private String key;
	private String value;
	
	public ProjectMetaData() {
		// jpa only
	}
	
	public ProjectMetaData(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public void setValue(String value) {
		this.value = value;
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
		
		if (other.key == null || other.value == null) {
			return false;
		}
		
		return other.key.equals(key) && other.value.equals(value);  
	}

	@Override
	public String toString() {
		return "ProjectMetaData [key=" + key + ", value=" + value + "]";
	}

}
