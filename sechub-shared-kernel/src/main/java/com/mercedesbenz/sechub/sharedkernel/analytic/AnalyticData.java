package com.mercedesbenz.sechub.sharedkernel.analytic;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This configuration is used by communication between different domain layers and the statistic domain")
public class AnalyticData implements JSONable<AnalyticData> {

    private UUID executionUUID;
    private CodeAnalyticData linesOfCode;

    @Override
    public Class<AnalyticData> getJSONTargetClass() {
        return AnalyticData.class;
    }

    public void setExecutionUUID(UUID executionUUID) {
        this.executionUUID = executionUUID;
    }

    public UUID getExecutionUUID() {
        return executionUUID;
    }

    public CodeAnalyticData getCodeAnalyticData() {
        return linesOfCode;
    }

    public void setLinesOfCode(CodeAnalyticData linesOfCode) {
        this.linesOfCode = linesOfCode;
    }

}
