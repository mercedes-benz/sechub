package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProviderFactory;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.checkmarx.CheckmarxWrapperEnvironment;

@Component
public class CheckmarxWrapperContextFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CheckmarxWrapperContextFactory.class);

    @Autowired
    NamePatternIdProviderFactory providerFactory;

    public CheckmarxWrapperContext create(CheckmarxWrapperEnvironment environment) {

        SecHubConfigurationModel configuration = createModel(environment.getSechubConfigurationModelAsJson());

        CheckmarxWrapperContext result = new CheckmarxWrapperContext(configuration, environment, providerFactory);

        return result;
    }

    private SecHubConfigurationModel createModel(String json) {
        try {
            return JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);

        } catch (JSONConverterException e) {
            LOG.error("Cannot convert given sechub configuration model", e);
        }
        LOG.warn("Because not being able to build correct sechub configuration model an empty model will be used as a fallback!");
        return new SecHubConfigurationModel(); // fallback - empty model
    }

}
