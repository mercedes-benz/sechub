package com.mercedesbenz.sechub.wrapper.infralight.product;

import com.mercedesbenz.sechub.commons.model.Severity;

/**
 * A simple class to contain minimum data from any infra scan product - used as
 * interchange format (simpler than sarif) inside Infralight wrapper.
 */
public class InfralightProductImportData {

    private Severity severity;

    private Integer cweId;

    private String description;

    public InfralightProductImportData(Severity severity, Integer cweId, String description) {
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
