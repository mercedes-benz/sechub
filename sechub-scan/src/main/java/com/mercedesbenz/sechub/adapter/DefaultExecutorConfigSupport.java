// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.environment.SystemEnvironmentVariableSupport;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProvider;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProviderFactory;
import com.mercedesbenz.sechub.commons.model.SecHubRuntimeException;
import com.mercedesbenz.sechub.domain.scan.product.ProductExecutorContext;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.mercedesbenz.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.mercedesbenz.sechub.sharedkernel.validation.AssertValidation;
import com.mercedesbenz.sechub.sharedkernel.validation.Validation;

/**
 * A standard executor configuration support. Supports environment entry
 * evaluation, simple key value checks etc.
 *
 * @author Albert Tregnaghi
 *
 */
public class DefaultExecutorConfigSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExecutorConfigSupport.class);

    private static final NamePatternIdProvider FALLBACK_NOT_FOUND_PROVIDER = new NamePatternIdProvider("fallback");

    protected Map<String, String> configuredExecutorParameters = new TreeMap<>();
    private Map<String, NamePatternIdProvider> namePatternIdProviders = new TreeMap<>();
    private SystemEnvironmentVariableSupport variableSupport;

    protected ProductExecutorConfig config;

    NamePatternIdProviderFactory providerFactory;

    public DefaultExecutorConfigSupport(ProductExecutorContext context, SystemEnvironmentVariableSupport variableSupport,
            Validation<ProductExecutorConfig> validation) {
        notNull(context, "context may not be null!");
        notNull(context.getExecutorConfig(), "executor config may not be null!");
        notNull(variableSupport, "variableSupport may not be null!");

        this.config = context.getExecutorConfig();
        this.variableSupport = variableSupport;

        providerFactory = new NamePatternIdProviderFactory();

        if (validation != null) {
            AssertValidation.assertValid(config, validation);
        }

        /* create a simple map containing parameters */
        List<ProductExecutorConfigSetupJobParameter> jobParameters = config.getSetup().getJobParameters();
        for (ProductExecutorConfigSetupJobParameter jobParameter : jobParameters) {
            configuredExecutorParameters.put(jobParameter.getKey(), jobParameter.getValue());
        }

    }

    public String getPasswordOrAPIToken() {
        ProductExecutorConfigSetupCredentials credentials = config.getSetup().getCredentials();
        String pwd = credentials.getPassword();
        return variableSupport.getValueOrVariableContent(pwd);
    }

    public String getProductBaseURL() {
        return config.getSetup().getBaseURL();
    }

    public String getUser() {
        ProductExecutorConfigSetupCredentials credentials = config.getSetup().getCredentials();
        String user = credentials.getUser();
        return variableSupport.getValueOrVariableContent(user);
    }

    /**
     * Get parameter boolean value for given key
     *
     * @param key
     * @return <code>true</code> when value for given key is "true" or "TRUE",
     *         otherwise false
     */
    protected boolean getParameterBooleanValue(String key) {
        String asText = getParameter(key);
        return Boolean.parseBoolean(asText);
    }

    /**
     * Get parameter string value for given key
     *
     * @param key
     * @return string or <code>null</code>
     */
    protected String getParameter(String key) {
        if (key == null) {
            return null;
        }
        return configuredExecutorParameters.get(key);
    }

    /**
     * Get parameter integer value for given key
     *
     * @param key
     * @return integer value or -1 if not defined
     */
    protected int getParameterIntValue(String key) {
        String asText = getParameter(key);
        if (asText == null) {
            return -1;
        }
        try {
            return Integer.parseInt(asText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Get parameter long value for given key
     *
     * @param key
     * @return long value or -1 if not defined
     */
    protected long getParameterLongValue(String key) {
        String asText = getParameter(key);
        if (asText == null) {
            return -1;
        }
        try {
            return Integer.parseInt(asText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Resolves a name pattern provider for given id
     *
     * @param id
     * @return provider never <code>null</code>
     * @throws SecHubRuntimeException when name pattern provider cannot be resolved
     */
    public NamePatternIdProvider getNamePatternIdProvider(String id) {
        return getNamePatternIdProvider(id, true);
    }

    /**
     * Resolves a name pattern provider for given id
     *
     * @param id
     * @param failWhenNotConfigured when <code>false</code> missing name provider
     *                              will be replaced by fallback implementation
     *                              returning always null (nothing configured)
     * @return provider never <code>null</code>
     * @throws SecHubRuntimeException when name pattern provider cannot be resolved
     */
    public NamePatternIdProvider getNamePatternIdProvider(String id, boolean failWhenNotConfigured) {
        NamePatternIdProvider provider = namePatternIdProviders.get(id);
        if (provider != null) {
            return provider;
        }

        String parameterValue = getParameter(id);
        if (parameterValue == null) {
            if (failWhenNotConfigured) {
                throw new SecHubRuntimeException("No parameter found for necessary mapping key:" + id);
            } else {
                return FALLBACK_NOT_FOUND_PROVIDER;
            }
        }
        NamePatternIdProvider newProvider = providerFactory.createProvider(id, parameterValue);
        namePatternIdProviders.put(id, newProvider);

        LOG.debug("Created NamePatternIdProvider:{}", newProvider.getProviderId());
        return newProvider;
    }

}
