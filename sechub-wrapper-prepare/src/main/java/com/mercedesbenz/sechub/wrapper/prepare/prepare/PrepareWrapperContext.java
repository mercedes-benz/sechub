package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import com.mercedesbenz.sechub.commons.model.RemoteCredentialContainer;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

public class PrepareWrapperContext {

    private SecHubConfigurationModel secHubConfiguration;
    private RemoteCredentialContainer remoteCredentialContainer;
    private PrepareWrapperEnvironment environment;

    public PrepareWrapperContext(SecHubConfigurationModel secHubConfiguration, RemoteCredentialContainer remoteCredentialContainer,
            PrepareWrapperEnvironment environment) {
        this.secHubConfiguration = secHubConfiguration;
        this.remoteCredentialContainer = remoteCredentialContainer;
        this.environment = environment;
    }

    public SecHubConfigurationModel getSecHubConfiguration() {
        return secHubConfiguration;
    }

    public RemoteCredentialContainer getRemoteCredentialContainer() {
        return remoteCredentialContainer;
    }

    public PrepareWrapperEnvironment getEnvironment() {
        return environment;
    }
}
