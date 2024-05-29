// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorConfigurationModel;
import com.mercedesbenz.sechub.wrapper.secret.validator.properties.SecretValidatorProxySettings;

import de.jcup.sarif_2_1_0.model.SarifSchema210;

public class SecretValidatorExecutionContext {

    private SarifSchema210 sarifReport;

    private Map<String, SecretValidatorConfigurationModel> validatorConfiguration = new HashMap<>();

    private boolean trustAllCertificates;

    private SecretValidatorProxySettings proxy;

    private SecretValidatorExecutionContext() {
    }

    public SarifSchema210 getSarifReport() {
        return sarifReport;
    }

    public Map<String, SecretValidatorConfigurationModel> getValidatorConfiguration() {
        return Collections.unmodifiableMap(validatorConfiguration);
    }

    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    public SecretValidatorProxySettings getProxy() {
        return proxy;
    }

    public static SecretValidatorExecutionContextBuilder builder() {
        return new SecretValidatorExecutionContextBuilder();
    }

    public static class SecretValidatorExecutionContextBuilder {

        private SarifSchema210 sarifReport;

        private Map<String, SecretValidatorConfigurationModel> validatorConfiguration = new HashMap<>();

        private boolean trustAllCertificates;

        private SecretValidatorProxySettings proxy;

        public SecretValidatorExecutionContextBuilder setSarifReport(SarifSchema210 report) {
            this.sarifReport = report;
            return this;
        }

        public SecretValidatorExecutionContextBuilder setValidatorConfiguration(Map<String, SecretValidatorConfigurationModel> validatorConfiguration) {
            this.validatorConfiguration = validatorConfiguration;
            return this;
        }

        public SecretValidatorExecutionContextBuilder setTrustAllCertificates(boolean trustAllCertificates) {
            this.trustAllCertificates = trustAllCertificates;
            return this;
        }

        public SecretValidatorExecutionContextBuilder setProxy(SecretValidatorProxySettings proxy) {
            this.proxy = proxy;
            return this;
        }

        public SecretValidatorExecutionContext build() {
            SecretValidatorExecutionContext secretValidatorExecutionContext = new SecretValidatorExecutionContext();
            secretValidatorExecutionContext.sarifReport = this.sarifReport;
            secretValidatorExecutionContext.validatorConfiguration = this.validatorConfiguration;
            secretValidatorExecutionContext.trustAllCertificates = this.trustAllCertificates;
            secretValidatorExecutionContext.proxy = this.proxy;

            return secretValidatorExecutionContext;
        }

    }
}
