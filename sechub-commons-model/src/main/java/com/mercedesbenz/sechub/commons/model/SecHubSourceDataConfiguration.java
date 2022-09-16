// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubSourceDataConfiguration extends AbstractSecHubFileSystemContainer implements SecHubDataConfigurationObject {

    private String uniqueName;

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }

}
