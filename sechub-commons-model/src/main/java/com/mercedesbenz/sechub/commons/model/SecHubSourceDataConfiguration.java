// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecHubSourceDataConfiguration extends AbstractSecHubFileSystemContainer implements SecHubDataConfigurationObject, SecHubRemoteContainer {

    private String uniqueName;

    private SecHubRemoteDataConfiguration remote;

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public void setRemote(SecHubRemoteDataConfiguration remote) {
        this.remote = remote;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }

    @Override
    public Optional<SecHubRemoteDataConfiguration> getRemote() {
        return Optional.ofNullable(remote);
    }

}
