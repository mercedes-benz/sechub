package com.mercedesbenz.sechub.sharedkernel.analytic;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class AnalyticProductData {

    private String name;
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String productName) {
        this.name = productName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
