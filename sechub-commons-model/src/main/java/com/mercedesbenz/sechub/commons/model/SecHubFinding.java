// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWeb;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubFinding implements Comparable<SecHubFinding> {

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

    String request;

    String solution;
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

    SecHubReportWeb web;

    public void setType(ScanType scanType) {
        this.type = scanType;
    }

    public ScanType getType() {
        return type;
    }

    public SecHubReportWeb getWeb() {
        return web;
    }

    public void setWeb(SecHubReportWeb web) {
        this.web = web;
    }

    /**
     * CVE result - interesting for infra scans
     *
     * @return CVE id - e.g. "CVE-2014-9999999" see https://cve.mitre.org/ or
     *         <code>null</code>
     */
    public String getCveId() {
        return cveId;
    }

    /***
     * Set CVE id - e.g. "CVE-2014-9999999" see https://cve.mitre.org/
     *
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
     *
     * @return common vulnerability enumeration id - see https://cwe.mitre.org/ or
     *         <code>null</code> when not defined
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

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
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

    public boolean hasScanType(ScanType scanType) {
        String typeAsString = null;
        if (scanType != null) {
            typeAsString = scanType.getId();
        }
        return hasScanType(typeAsString);
    }

    public boolean hasScanType(String typeAsString) {
        if (typeAsString == null) {
            return false;
        }
        if (this.type == null) {
            return false;
        }

        String typeId = this.type.getId();
        return typeAsString.equalsIgnoreCase(typeId);
    }

    @Override
    public int compareTo(SecHubFinding o) {
        if (o == null) {
            return 1;
        }
        int otherSeverityLevel = 0;
        int severityLevel = 0;

        Severity otherSeverity = o.severity;
        if (otherSeverity != null) {
            otherSeverityLevel = otherSeverity.getLevel();
        }
        if (severity != null) {
            severityLevel = severity.getLevel();
        }

        int severityCompare = otherSeverityLevel - severityLevel;
        if (severityCompare != 0) {
            return severityCompare;
        }

        int otherCweInt = Integer.MAX_VALUE;
        int cweInt = Integer.MAX_VALUE;
        if (o.cweId != null) {
            otherCweInt = o.cweId;
        }
        if (cweId != null) {
            cweInt = cweId;
        }
        int cweCompare = cweInt - otherCweInt;
        if (cweCompare != 0) {
            return cweCompare;
        }

        String cveString = "_";
        String otherCveString = "_";

        if (o.cveId != null) {
            otherCveString = o.cveId;
        }
        if (cveId != null) {
            cveString = cveId;
        }
        int cveCompare = cveString.compareTo(otherCveString);
        return cveCompare;
    }

    @Override
    public String toString() {
        return "SecHubFinding: id:" + id + ", severity:" + severity + ", name:" + name + ", cweId:" + cweId + ", cveId:" + cveId + ",scanType:" + type;
    }

}
