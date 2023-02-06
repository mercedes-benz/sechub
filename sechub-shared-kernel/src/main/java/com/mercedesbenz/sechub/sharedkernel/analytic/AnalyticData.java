package com.mercedesbenz.sechub.sharedkernel.analytic;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@MustBeKeptStable("This configuration is used by communication between different domain layers and the statistic domain")
public class AnalyticData implements JSONable<AnalyticData> {

    private CodeAnalyticData codeAnalyticData;

    @Override
    public Class<AnalyticData> getJSONTargetClass() {
        return AnalyticData.class;
    }

    public void setCodeAnalyticData(CodeAnalyticData codeAnalyticData) {
        this.codeAnalyticData = codeAnalyticData;
    }
    
    public Optional<CodeAnalyticData> getCodeAnalyticData() {
        return Optional.ofNullable(codeAnalyticData);
    }
    

}
