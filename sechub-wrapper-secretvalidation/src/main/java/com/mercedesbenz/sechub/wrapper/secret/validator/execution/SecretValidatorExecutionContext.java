// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorConfigurationModel;

import de.jcup.sarif_2_1_0.model.SarifSchema210;

public class SecretValidatorExecutionContext {

    private SarifSchema210 sarifReport;

    private Map<String, SecretValidatorConfigurationModel> validatorConfiguration = new HashMap<>();

    private int maximumRetries;

    private SecretValidatorExecutionContext() {
    }

    public SarifSchema210 getSarifReport() {
        return sarifReport;
    }

    public Map<String, SecretValidatorConfigurationModel> getValidatorConfiguration() {
        return Collections.unmodifiableMap(validatorConfiguration);
    }

    public int getMaximumRetries() {
        return maximumRetries;
    }

    public static SecretValidatorExecutionContextBuilder builder() {
        return new SecretValidatorExecutionContextBuilder();
    }

    public static class SecretValidatorExecutionContextBuilder {

        private SarifSchema210 sarifReport;

        private Map<String, SecretValidatorConfigurationModel> validatorConfiguration = new HashMap<>();

        private int maximumRetries;

        public SecretValidatorExecutionContextBuilder setSarifReport(SarifSchema210 report) {
            this.sarifReport = report;
            return this;
        }

        public SecretValidatorExecutionContextBuilder setValidatorConfiguration(Map<String, SecretValidatorConfigurationModel> validatorConfiguration) {
            this.validatorConfiguration = validatorConfiguration;
            return this;
        }

        public SecretValidatorExecutionContextBuilder setMaximumRetries(int maximumRetries) {
            this.maximumRetries = maximumRetries;
            return this;
        }

        public SecretValidatorExecutionContext build() {
            SecretValidatorExecutionContext secretValidatorExecutionContext = new SecretValidatorExecutionContext();
            secretValidatorExecutionContext.sarifReport = this.sarifReport;
            secretValidatorExecutionContext.validatorConfiguration = this.validatorConfiguration;
            secretValidatorExecutionContext.maximumRetries = this.maximumRetries;

            return secretValidatorExecutionContext;
        }

    }
}
