// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.commons.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PDSProductParameterDefinition {

    private String key;

    private String description;

    private String _default; // default is java keyword => "_" as prefix was necessary...

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefault() {
        return _default;
    }

    public void setDefault(String defaultValue) {
        this._default = defaultValue;
    }

    public boolean hasDefault() {
        return _default != null;
    }
}
