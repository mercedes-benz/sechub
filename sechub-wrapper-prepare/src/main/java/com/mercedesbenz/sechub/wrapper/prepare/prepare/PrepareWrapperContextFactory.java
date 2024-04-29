package com.mercedesbenz.sechub.wrapper.prepare.prepare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.*;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

@Component
public class PrepareWrapperContextFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperContextFactory.class);

    public PrepareWrapperContext create(PrepareWrapperEnvironment environment) {
        SecHubConfigurationModel secHubConfigModel = createSecHubConfigModel(environment.getSechubConfigurationModelAsJson());
        return new PrepareWrapperContext(secHubConfigModel, environment);
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
}