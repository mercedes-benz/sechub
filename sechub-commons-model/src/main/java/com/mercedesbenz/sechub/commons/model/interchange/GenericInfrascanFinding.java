package com.mercedesbenz.sechub.commons.model.interchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.Severity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericInfrascanFinding {

    private String target;

    private Integer cweId;
    private String cveId;
    private String name;

    private String description;

    private String solution;
    
    private Severity severity;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    
    public String getCveId() {
        return cveId;
    }
    
    public void setCveId(String cveId) {
        this.cveId = cveId;
    }
    
    public Integer getCweId() {
        return cweId;
    }

    public void setCweId(Integer cweId) {
        this.cweId = cweId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    

}
