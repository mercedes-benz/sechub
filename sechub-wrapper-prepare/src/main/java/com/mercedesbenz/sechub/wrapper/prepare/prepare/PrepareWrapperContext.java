package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

public class PrepareWrapperContext {

    private SecHubConfigurationModel secHubConfiguration;
    private PrepareWrapperEnvironment environment;

    public PrepareWrapperContext(SecHubConfigurationModel secHubConfiguration, PrepareWrapperEnvironment environment) {
        this.secHubConfiguration = secHubConfiguration;
        this.environment = environment;
    }

    public SecHubConfigurationModel getSecHubConfiguration() {
        return secHubConfiguration;
    }

    public PrepareWrapperEnvironment getEnvironment() {
        return environment;
    }
}
