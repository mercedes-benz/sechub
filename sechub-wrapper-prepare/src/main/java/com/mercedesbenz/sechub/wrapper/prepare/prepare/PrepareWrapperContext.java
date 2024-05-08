// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubRemoteDataConfiguration;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

public class PrepareWrapperContext {

    private SecHubConfigurationModel secHubConfiguration;
    private PrepareWrapperEnvironment environment;
    private List<SecHubRemoteDataConfiguration> remoteDataConfigurationList = new ArrayList<>();
    private List<SecHubMessage> userMessages = new ArrayList<>();

    public PrepareWrapperContext(SecHubConfigurationModel secHubConfiguration, PrepareWrapperEnvironment environment) {
        this.secHubConfiguration = secHubConfiguration;
        this.environment = environment;
    }

    public void setRemoteDataConfigurationList(List<SecHubRemoteDataConfiguration> remoteDataConfigurationList) {
        this.remoteDataConfigurationList = remoteDataConfigurationList;
    }

    public List<SecHubRemoteDataConfiguration> getRemoteDataConfigurationList() {
        return remoteDataConfigurationList;
    }

    public SecHubConfigurationModel getSecHubConfiguration() {
        return secHubConfiguration;
    }

    public PrepareWrapperEnvironment getEnvironment() {
        return environment;
    }

    public List<SecHubMessage> getUserMessages() {
        return userMessages;
    }
}
