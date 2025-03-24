// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.prepare.cli.PrepareWrapperEnvironment;

@Component
public class PrepareWrapperContextFactory {

    private final PrepareWrapperRemoteConfigurationExtractor extractor;
    private static final Logger LOG = LoggerFactory.getLogger(PrepareWrapperContextFactory.class);

    public PrepareWrapperContextFactory(PrepareWrapperRemoteConfigurationExtractor extractor) {
        this.extractor = extractor;
    }

    public PrepareWrapperContext create(PrepareWrapperEnvironment environment) {
        SecHubConfigurationModel secHubConfigModel = createSecHubConfigModel(environment.getSechubConfigurationModelAsJson());
        PrepareWrapperContext context = new PrepareWrapperContext(secHubConfigModel, environment);
        addRemoteDataConfiguration(context);
        return context;
    }

    private SecHubConfigurationModel createSecHubConfigModel(String json) {
        if (json == null || json.isEmpty()) {
            throw new IllegalStateException("No SecHub model JSON found, cannot create model.");
        }
        try {
            return JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);
        } catch (JSONConverterException e) {
            LOG.error("Cannot convert given sechub configuration model.", e);
            throw new IllegalStateException("SecHub model JSON found, but is invalid.", e);
        }
    }

    private void addRemoteDataConfiguration(PrepareWrapperContext context) {
        context.setRemoteDataConfiguration(extractor.extract(context.getSecHubConfiguration()));
    }
}
