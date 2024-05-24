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
    private List<SecHubMessage> userMessages = new ArrayList<>();
    private SecHubRemoteDataConfiguration remoteDataConfiguration;

    public PrepareWrapperContext(SecHubConfigurationModel secHubConfiguration, PrepareWrapperEnvironment environment) {
        this.secHubConfiguration = secHubConfiguration;
        this.environment = environment;
    }

    public void setRemoteDataConfiguration(SecHubRemoteDataConfiguration remoteDataConfiguration) {
        this.remoteDataConfiguration = remoteDataConfiguration;
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

    public SecHubRemoteDataConfiguration getRemoteDataConfiguration() {
        return remoteDataConfiguration;
    }
}
