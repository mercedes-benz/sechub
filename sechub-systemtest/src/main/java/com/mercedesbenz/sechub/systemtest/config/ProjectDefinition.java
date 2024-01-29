// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

import java.util.ArrayList;
import java.util.List;

public class ProjectDefinition extends AbstractDefinition {

    private String name;
    private List<String> profiles = new ArrayList<>();
    private List<String> whitelistedURIs = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public List<String> getWhitelistedURIs() {
        return whitelistedURIs;
    }
}
