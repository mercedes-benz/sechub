package com.daimler.sechub.domain.scan.product.pds;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

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

public class PDSExecutionConfigSuppport {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionConfigSuppport.class);

    private Map<String, String> parametersAsMap = new TreeMap<>();
    private ProductExecutorConfig config;
    private SystemEnvironment systemEnvironment;

    public PDSExecutionConfigSuppport(ProductExecutorConfig config, SystemEnvironment systemEnvironment) {
        notNull(config, "config may not be null!");
        notNull(systemEnvironment, "systemEnvironment may not be null!");

        this.config = config;
        this.systemEnvironment = systemEnvironment;

        List<ProductExecutorConfigSetupJobParameter> jobParameters = config.getSetup().getJobParameters();
        for (ProductExecutorConfigSetupJobParameter jobParameter : jobParameters) {
            parametersAsMap.put(jobParameter.getKey(), jobParameter.getValue());
        }
    }

    public int getScanResultCheckTimeoutInMinutes(PDSInstallSetup setup) {
        int value = getParameterIntValue(PDSSecHubDataKeys.TIME_TO_WAIT_BEFORE_TIMEOUT);
        if (value != -1) {
            return value;
        }
        /* fallback to setup */
        return setup.getDefaultScanResultCheckPeriodInMinutes();
    }

    public int getScanResultCheckPeriodInMinutes(PDSInstallSetup setup) {
        int value = getParameterIntValue(PDSSecHubDataKeys.TIME_TO_WAIT_FOR_NEXT_CHECKOPERATION);
        if (value != -1) {
            return value;
        }
        /* fallback to setup */
        return setup.getDefaultScanResultCheckPeriodInMinutes();
    }

    public boolean isTargetTypeForbidden(TargetType targetType) {
        boolean forbidden = false;
        for (PDSSecHubDataKeys k : PDSSecHubDataKeys.values()) {
            if (forbidden) {
                break;
            }
            PDSSecHubConfigDataKey forbiddenKey = k.getKey();
            if (!(forbiddenKey instanceof PDSForbiddenTargetTypeInputKey)) {
                continue;
            }
            String val = getParameter(forbiddenKey);
            forbidden = Boolean.parseBoolean(val);
        }
        return forbidden;
    }

    private int getParameterIntValue(PDSSecHubDataKeys k) {
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

    private String getParameter(PDSSecHubDataKeys k) {
        return getParameter(k.getKey());
    }

    private String getParameter(PDSSecHubConfigDataKey key) {
        return parametersAsMap.get(key.getId());
    }

    public Map<String, String> createJobParametersToSendToPDS() {
        Map<String, String> parametersToSend = new TreeMap<>();
        parametersToSend.putAll(parametersAsMap); // just add all config values to result
        return parametersToSend;
    }

    public String getUser() {
        ProductExecutorConfigSetupCredentials credentials = config.getSetup().getCredentials();
        String user = credentials.getUser();
        return evaluateEnvironmentEntry(user);
    }

    public String getPasswordOrAPIToken() {
        ProductExecutorConfigSetupCredentials credentials = config.getSetup().getCredentials();
        String pwd = credentials.getPassword();
        return evaluateEnvironmentEntry(pwd);
    }
    
    private String evaluateEnvironmentEntry(String data) {
        if (data==null) {
            return null;
        }
        if (!data.startsWith("env:")) {
            return data;
        }
        String key = data.substring("env:".length());
        String value = systemEnvironment.getEnv(key);
        if (value==null) {
            LOG.warn("No environment entry defined for variable:{}",key);
        }
        return value;
    }

}
