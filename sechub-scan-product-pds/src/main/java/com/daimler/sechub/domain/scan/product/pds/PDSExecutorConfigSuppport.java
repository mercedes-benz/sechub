// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.daimler.sechub.adapter.DefaultExecutorConfigSupport;
import com.daimler.sechub.commons.core.util.SecHubStorageUtil;
import com.daimler.sechub.commons.pds.PDSConfigDataKeyProvider;
import com.daimler.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.daimler.sechub.commons.pds.PDSKey;
import com.daimler.sechub.commons.pds.PDSKeyProvider;
import com.daimler.sechub.domain.scan.TargetType;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.sharedkernel.SystemEnvironment;
import com.daimler.sechub.sharedkernel.configuration.SecHubConfiguration;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.validation.Validation;

public class PDSExecutorConfigSuppport extends DefaultExecutorConfigSupport {

    /**
     * Creates the configuration support and VALIDATE. This will fail when
     * configuration data is not valid (e.g. mandatory keys missing)
     *
     * @param config
     * @param systemEnvironment
     * @return support
     * @throws NotAcceptableException when configuration is not valid
     */
    public static PDSExecutorConfigSuppport createSupportAndAssertConfigValid(ProductExecutorConfig config, SystemEnvironment systemEnvironment) {
        return new PDSExecutorConfigSuppport(config, systemEnvironment, new PDSProductExecutorMinimumConfigValidation());
    }

    private PDSExecutorConfigSuppport(ProductExecutorConfig config, SystemEnvironment systemEnvironment, Validation<ProductExecutorConfig> validation) {
        super(config, systemEnvironment, validation);
    }

    public Map<String, String> createJobParametersToSendToPDS(SecHubConfiguration secHubConfiguration) {

        Map<String, String> parametersToSend = new TreeMap<>();
        List<PDSKeyProvider<?>> providers = new ArrayList<>();
        providers.addAll(Arrays.asList(SecHubProductExecutionPDSKeyProvider.values()));
        providers.addAll(Arrays.asList(PDSConfigDataKeyProvider.values()));

        for (String originKey : configuredExecutorParameters.keySet()) {
            PDSKeyProvider<?> foundProvider = null;
            for (PDSKeyProvider<?> provider : providers) {
                String key = provider.getKey().getId();
                if (originKey.equalsIgnoreCase(key)) {
                    foundProvider = provider;
                    break;
                }
            }
            /* either not special (so always sent to PDS) or special but must be sent */
            if (foundProvider == null || foundProvider.getKey().isSentToPDS()) {
                parametersToSend.put(originKey, configuredExecutorParameters.get(originKey));
            }
        }
        /* provide SecHub storage when necessary */
        String useSecHubStorage = parametersToSend.get(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_USE_SECHUB_STORAGE);
        if (Boolean.parseBoolean(useSecHubStorage)) {
            String projectId = secHubConfiguration.getProjectId();
            String sechubStoragePath = SecHubStorageUtil.createStoragePath(projectId);

            parametersToSend.put(PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_CONFIG_SECHUB_STORAGE_PATH, sechubStoragePath);
        }

        return parametersToSend;
    }

    public String getPDSProductIdentifier() {
        return getParameter(PDSConfigDataKeyProvider.PDS_CONFIG_PRODUCTIDENTIFIER);
    }

    public int getTimeToWaitForNextCheckOperationInMilliseconds(PDSInstallSetup setup) {
        int value = getParameterIntValue(SecHubProductExecutionPDSKeyProvider.TIME_TO_WAIT_FOR_NEXT_CHECKOPERATION_IN_MILLISECONDS);
        if (value != -1) {
            return value;
        }
        /* fallback to setup */
        return setup.getDefaultTimeToWaitForNextCheckOperationInMilliseconds();
    }

    public int getTimeoutInMinutes(PDSInstallSetup setup) {
        int value = getParameterIntValue(SecHubProductExecutionPDSKeyProvider.TIME_TO_WAIT_BEFORE_TIMEOUT_IN_MINUTES);
        if (value != -1) {
            return value;
        }
        /* fallback to setup */
        return setup.getDefaultTimeOutInMinutes();
    }

    /**
     * @return <code>true</code> when PDS server with an untrusted certificate (e.g.
     *         self signed) is accepted, <code>false</code> when not (default)
     */
    public boolean isTrustAllCertificatesEnabled() {
        return getParameterBooleanValue(SecHubProductExecutionPDSKeyProvider.TRUST_ALL_CERTIFICATES);
    }

    public boolean isTargetTypeForbidden(TargetType targetType) {
        boolean forbidden = false;
        for (SecHubProductExecutionPDSKeyProvider provider : SecHubProductExecutionPDSKeyProvider.values()) {
            if (forbidden) {
                break;
            }
            PDSKey forbiddenKey = provider.getKey();
            if (!(forbiddenKey instanceof ForbiddenTargetTypePDSKey)) {
                continue;
            }
            ForbiddenTargetTypePDSKey pdsForbiddenKey = (ForbiddenTargetTypePDSKey) forbiddenKey;
            if (!targetType.equals(pdsForbiddenKey.getForbiddenTargetType())) {
                continue;
            }
            String val = getParameter(forbiddenKey);
            forbidden = Boolean.parseBoolean(val);
        }
        return forbidden;
    }

    private String getParameter(PDSKeyProvider<?> keyProvider) {
        return getParameter(keyProvider.getKey());
    }

    private String getParameter(PDSKey configDataKey) {
        return getParameter(configDataKey.getId());
    }

    private int getParameterIntValue(PDSKeyProvider<? extends PDSKey> provider) {
        return getParameterIntValue(provider.getKey().getId());
    }

    private boolean getParameterBooleanValue(PDSKeyProvider<? extends PDSKey> provider) {
        return getParameterBooleanValue(provider.getKey().getId());
    }

}
