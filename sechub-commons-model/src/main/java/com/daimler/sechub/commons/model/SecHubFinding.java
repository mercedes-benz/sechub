// SPDX-License-Identifier: MIT
package com.daimler.sechub.commons.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class SecHubFinding {

	int id;

	String description;

	List<String> hostnames = new ArrayList<>();

	Date created;

	String createdBy;

	String name;

	String parameters;

	String path;

	String parameterName;

	String query;

	List<String> references = new ArrayList<>();

	String method;
	String request;

	String resolution;
	String response;
	String service;
	Severity severity;
	String target;

	String website;

	SecHubCodeCallStack code;

	String productResultLink;
	
	ScanType type;

    Integer cweId;
    
    String cveId;
    
    String owasp;
	
	public void setType(ScanType scanType) {
        this.type = scanType;
    }
	
	public ScanType getType() {
        return type;
    }
	
	/**
	 * CVE result - interesting for infra scans
	 * @return CVE id - e.g. "CVE-2014-9999999" see https://cve.mitre.org/ or <code>null</code>
	 */
	public String getCveId() {
        return cveId;
    }
	
	/***
	 * Set CVE id - e.g. "CVE-2014-9999999" see https://cve.mitre.org/
	 * @param cveId
	 */
	public void setCveId(String cveId) {
        this.cveId = cveId;
    }
	
	public void setCweId(Integer cweId) {
        this.cweId = cweId;
    }
	
	/**
	 * CWE result - interesting for code scans, web scans
	 * @return common vulnerability enumeration id - see https://cwe.mitre.org/ or <code>null</code> when not defined
	 */
	public Integer getCweId() {
        return cweId;
    }
	
	public void setProductResultLink(String productResultLink) {
		this.productResultLink = productResultLink;
	}

	public String getProductResultLink() {
		return productResultLink;
	}

	public void setCode(SecHubCodeCallStack code) {
		this.code = code;
	}

	public SecHubCodeCallStack getCode() {
		return code;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getHostnames() {
		return hostnames;
	}

	public void setHostnames(List<String> hostnames) {
		this.hostnames = hostnames;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<String> getReferences() {
		return references;
	}

	public void setReferences(List<String> references) {
		this.references = references;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getWebsite() {
		return website;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}
}
