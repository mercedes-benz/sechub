package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

@Component
public class PrepareWrapperContextFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperContextFactory.class);

    @Autowired
    RemoteCredentialContainerFactory remoteCredentialContainerFactory;

    public PrepareWrapperContext create(PrepareWrapperEnvironment environment) {
        SecHubConfigurationModel secHubConfigModel = createSecHubConfigModel(environment.getSechubConfigurationModelAsJson());
        RemoteCredentialConfiguration remoteCredentialConfig = createRemoteConfigModel(environment.getRemoteCredentialConfigurationAsJSON());
        RemoteCredentialContainer remoteCredentialContainer = remoteCredentialContainerFactory.create(remoteCredentialConfig);

        return new PrepareWrapperContext(secHubConfigModel, remoteCredentialContainer, environment);
    }

    private SecHubConfigurationModel createSecHubConfigModel(String json) {
        if (json == null || json.isEmpty()) {
            throw new IllegalStateException("No SecHub model JSON found, cannot create model");
        }
        try {
            return JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);
        } catch (JSONConverterException e) {
            LOG.error("Cannot convert given sechub configuration model", e);
            throw new IllegalStateException("SecHub model JSON found, but invalid", e);
        }
    }

    private RemoteCredentialConfiguration createRemoteConfigModel(String json) {
        if (json == null || json.isEmpty()) {
            throw new IllegalStateException("No Remote Data Configuration model JSON found, cannot create model");
        }
        try {
            return RemoteCredentialConfiguration.fromJSONString(json);

        } catch (JSONConverterException e) {
            LOG.error("Cannot convert given sechub configuration model", e);
        }
        LOG.warn("Because not being able to build correct sechub configuration model an empty model will be used as a fallback!");
        return new RemoteCredentialConfiguration(); // fallback - empty model
    }
}
