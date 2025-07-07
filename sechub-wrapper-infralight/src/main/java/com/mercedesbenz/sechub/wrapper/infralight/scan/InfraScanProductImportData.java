package com.mercedesbenz.sechub.wrapper.infralight.scan;

import com.mercedesbenz.sechub.commons.model.Severity;

public class InfraScanProductImportData {
    
    private Severity severity;
    
    private Integer cweId;
    
    private String description;

    public InfraScanProductImportData(Severity severity, Integer cweId, String description) {
        super();
        this.severity = severity;
        this.cweId = cweId;
        this.description = description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Integer getCweId() {
        return cweId;
    }

    public String getDescription() {
        return description;
    }
    
    
}
