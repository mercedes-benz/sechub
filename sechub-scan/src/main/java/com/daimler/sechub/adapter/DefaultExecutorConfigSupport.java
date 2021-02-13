// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.commons.model.SecHubRuntimeException;
import com.daimler.sechub.domain.scan.NamePatternIdProviderFactory;
import com.daimler.sechub.domain.scan.config.NamePatternIdprovider;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.daimler.sechub.sharedkernel.SystemEnvironment;
import com.daimler.sechub.sharedkernel.validation.AssertValidation;
import com.daimler.sechub.sharedkernel.validation.Validation;

/**
 * A standard executor configuration support. Supports environment entry
 * evaluation, simple key value checks etc.
 * 
 * @author Albert Tregnaghi
 *
 */
public class DefaultExecutorConfigSupport {

    private static final String ENV_PREFIX_ID = "env:";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExecutorConfigSupport.class);

    private static final NamePatternIdprovider FALLBACK_NOT_FOUND_PROVIDER = new NamePatternIdprovider("fallback");

    protected Map<String, String> configuredExecutorParameters = new TreeMap<>();
    private Map<String, NamePatternIdprovider> namePatternIdProviders = new TreeMap<>();

    protected ProductExecutorConfig config;
    private SystemEnvironment systemEnvironment;
    NamePatternIdProviderFactory providerFactory;

    public DefaultExecutorConfigSupport(ProductExecutorConfig config, SystemEnvironment systemEnvironment, Validation<ProductExecutorConfig> validation) {
        notNull(config, "config may not be null!");
        notNull(systemEnvironment, "systemEnvironment may not be null!");

        this.config = config;
        this.systemEnvironment = systemEnvironment;

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
        return evaluateEnvironmentEntry(pwd);
    }

    public String getProductBaseURL() {
        return config.getSetup().getBaseURL();
    }

    public String getUser() {
        ProductExecutorConfigSetupCredentials credentials = config.getSetup().getCredentials();
        String user = credentials.getUser();
        return evaluateEnvironmentEntry(user);
    }

    private String evaluateEnvironmentEntry(String data) {
        if (data == null) {
            return null;
        }
        if (!data.startsWith(ENV_PREFIX_ID)) {
            return data;
        }
        String key = data.substring(ENV_PREFIX_ID.length());
        String value = systemEnvironment.getEnv(key);
        if (value == null) {
            LOG.warn("No environment entry defined for variable:{}", key);
        }
        return value;
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
     * Resolves a name pattern provider for given id
     * 
     * @param id
     * @return provider never <code>null</code>
     * @throws SecHubRuntimeException when name pattern provider cannot be resolved
     */
    public NamePatternIdprovider getNamePatternIdProvider(String id) {
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
    public NamePatternIdprovider getNamePatternIdProvider(String id, boolean failWhenNotConfigured) {
        NamePatternIdprovider provider = namePatternIdProviders.get(id);
        if (provider != null) {
            return provider;
        }

        String parameterValue = getParameter(id);
        if (parameterValue == null) {
            if (failWhenNotConfigured) {
                throw new SecHubRuntimeException("No parameter found for necessary mapping key:" + id);
            }else {
                return FALLBACK_NOT_FOUND_PROVIDER;
            }
        }
        NamePatternIdprovider newProvider = providerFactory.createProvider(id, parameterValue);
        namePatternIdProviders.put(id, newProvider);

        LOG.debug("Created NamePatternIdprovider:{}", newProvider.getProviderId());
        return newProvider;
    }

}
