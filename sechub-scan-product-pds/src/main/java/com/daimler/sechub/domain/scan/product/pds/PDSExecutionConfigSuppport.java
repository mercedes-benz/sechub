// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupCredentials;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.daimler.sechub.sharedkernel.SystemEnvironment;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.validation.AssertValidation;

public class PDSExecutionConfigSuppport {

    private static final String ENV_PREFIX_ID = "env:";

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionConfigSuppport.class);

    private ProductExecutorConfig config;
    private Map<String, String> configuredExecutorParameters = new TreeMap<>();
    private SystemEnvironment systemEnvironment;

    private PDSProductExecutorMinimumConfigValidation validation;

    /**
     * Creates the configuration support and VALIDATE. This will fail when configuration data is not valid (e.g. mandatory keys missing)
     * 
     * @param config
     * @param systemEnvironment
     * @return support
     * @throws NotAcceptableException when configuration is not valid
     */
    public static PDSExecutionConfigSuppport createSupportAndAssertConfigValid(ProductExecutorConfig config, SystemEnvironment systemEnvironment) {
        PDSExecutionConfigSuppport support = new PDSExecutionConfigSuppport(config, systemEnvironment);
        support.assertValidConfiguration();
        
        return support;
    }

    private PDSExecutionConfigSuppport(ProductExecutorConfig config, SystemEnvironment systemEnvironment) {
        notNull(config, "config may not be null!");
        notNull(systemEnvironment, "systemEnvironment may not be null!");

        this.config = config;
        this.systemEnvironment = systemEnvironment;
        
        validation = new PDSProductExecutorMinimumConfigValidation();

        List<ProductExecutorConfigSetupJobParameter> jobParameters = config.getSetup().getJobParameters();
        for (ProductExecutorConfigSetupJobParameter jobParameter : jobParameters) {
            configuredExecutorParameters.put(jobParameter.getKey(), jobParameter.getValue());
        }

    }
    

    public Map<String, String> createJobParametersToSendToPDS() {
       
        Map<String, String> parametersToSend = new TreeMap<>();
        List<PDSSecHubConfigDataKeyProvider<?>> providers = new ArrayList<>();
        providers.addAll(Arrays.asList(PDSProductExecutorKeys.values()));
        providers.addAll(Arrays.asList(PDSConfigDataKeys.values()));
        
        for (String originKey  :configuredExecutorParameters.keySet()) {
            PDSSecHubConfigDataKeyProvider<?> foundProvider = null;
            for (PDSSecHubConfigDataKeyProvider<?> provider: providers) {
                String key = provider.getKey().getId();
                if (originKey.equalsIgnoreCase(key)) {
                    foundProvider = provider;
                    break;
                }
            }
            /* either not special (so always sent to PDS) or special but must be sent*/
            if (foundProvider==null || foundProvider.getKey().isSentToPDS()) {
                parametersToSend.put(originKey, configuredExecutorParameters.get(originKey));
            }
        }
        return parametersToSend;
    }
    
    public String getPasswordOrAPIToken() {
        ProductExecutorConfigSetupCredentials credentials = config.getSetup().getCredentials();
        String pwd = credentials.getPassword();
        return evaluateEnvironmentEntry(pwd);
    }

    public String getProductBaseURL() {
        return config.getSetup().getBaseURL();
    }
    
    public String getPDSProductIdentifier() {
        return getParameter(PDSConfigDataKeys.PDS_PRODUCT_IDENTIFIER);
    }

    public int getScanResultCheckPeriodInMinutes(PDSInstallSetup setup) {
        int value = getParameterIntValue(PDSProductExecutorKeys.TIME_TO_WAIT_FOR_NEXT_CHECKOPERATION);
        if (value != -1) {
            return value;
        }
        /* fallback to setup */
        return setup.getDefaultScanResultCheckPeriodInMinutes();
    }

    public int getScanResultCheckTimeoutInMinutes(PDSInstallSetup setup) {
        int value = getParameterIntValue(PDSProductExecutorKeys.TIME_TO_WAIT_BEFORE_TIMEOUT);
        if (value != -1) {
            return value;
        }
        /* fallback to setup */
        return setup.getDefaultScanResultCheckPeriodInMinutes();
    }

    public String getUser() {
        ProductExecutorConfigSetupCredentials credentials = config.getSetup().getCredentials();
        String user = credentials.getUser();
        return evaluateEnvironmentEntry(user);
    }

    public boolean isTrustAllCertificatesEnabled() {
        return getParameterBooleanValue(PDSProductExecutorKeys.TRUST_ALL_CERTRIFICATES);
    }
    
    public boolean isTargetTypeForbidden(TargetType targetType) {
        boolean forbidden = false;
        for (PDSProductExecutorKeys k : PDSProductExecutorKeys.values()) {
            if (forbidden) {
                break;
            }
            PDSSecHubConfigDataKey<?> forbiddenKey = k.getKey();
            if (!(forbiddenKey instanceof PDSForbiddenTargetTypeInputKey)) {
                continue;
            }
            String val = getParameter(forbiddenKey);
            forbidden = Boolean.parseBoolean(val);
        }
        return forbidden;
    }

    private void assertValidConfiguration() {
        AssertValidation.assertValid(config, validation);
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

    private String getParameter(PDSSecHubConfigDataKeyProvider<?> k) {
        return getParameter(k.getKey());
    }

    private String getParameter(PDSSecHubConfigDataKey<?> key) {
        return configuredExecutorParameters.get(key.getId());
    }

    private int getParameterIntValue(PDSProductExecutorKeys k) {
        String asText = getParameter(k);
        if (asText == null) {
            return -1;
        }
        try {
            return Integer.parseInt(asText);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private boolean getParameterBooleanValue(PDSProductExecutorKeys k) {
        String asText = getParameter(k);
        return Boolean.parseBoolean(asText);
    }

    
}
