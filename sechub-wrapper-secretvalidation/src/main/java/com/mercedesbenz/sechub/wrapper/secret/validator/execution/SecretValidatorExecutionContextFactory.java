// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorConfigurationModel;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorConfigurationModelList;
import com.mercedesbenz.sechub.wrapper.secret.validator.properties.SecretValidatorPDSJobResult;
import com.mercedesbenz.sechub.wrapper.secret.validator.properties.SecretValidatorProperties;

import de.jcup.sarif_2_1_0.model.SarifSchema210;

@Component
public class SecretValidatorExecutionContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SecretValidatorExecutionContextFactory.class);

    @Autowired
    SecretValidatorPDSJobResult pdsResult;

    @Autowired
    SecretValidatorProperties properties;

    TextFileReader reader = new TextFileReader();

    public SecretValidatorExecutionContext create() {
        LOG.info("Loading SARIF model from secret scan.");
        SarifSchema210 report = createSarifReport(pdsResult.getFile());

        LOG.info("Loading validator configuration for this pds solution.");
        Map<String, SecretValidatorConfigurationModel> ruleConfigurations = createRuleConfigurations(properties.getConfigFile());

        /* @formatter:off */
        SecretValidatorExecutionContext context =
                SecretValidatorExecutionContext.builder()
                                               .setSarifReport(report)
                                               .setValidatorConfiguration(ruleConfigurations)
                                               .setMaximumRetries(properties.getMaximumRetries())
                                               .build();
        /* @formatter:on */
        return context;
    }

    private SarifSchema210 createSarifReport(File pdsResultFile) {
        if (!pdsResultFile.exists()) {
            throw new IllegalStateException("PDS job result file: " + pdsResultFile + " does not exist!");
        } else if (!pdsResultFile.canRead()) {
            throw new IllegalStateException("PDS job result file: " + pdsResultFile + " is not readable!");
        }

        try {
            String sarifReportJson = reader.readTextFromFile(pdsResultFile);
            return JSONConverter.get().fromJSON(SarifSchema210.class, sarifReportJson);
        } catch (Exception e) {
            throw new IllegalStateException("Creating SARIF report model from: " + pdsResultFile + " failed!", e);
        }
    }

    private Map<String, SecretValidatorConfigurationModel> createRuleConfigurations(File validatorConfigFile) {
        if (!validatorConfigFile.exists()) {
            throw new IllegalStateException("Secret validator configuration file: " + validatorConfigFile + " does not exist!");
        } else if (!validatorConfigFile.canRead()) {
            throw new IllegalStateException("Secret validator configuration file: " + validatorConfigFile + " is not readable!");
        }

        try {
            String validatorConfigJson = reader.readTextFromFile(validatorConfigFile);
            SecretValidatorConfigurationModelList configurationDataList = JSONConverter.get().fromJSON(SecretValidatorConfigurationModelList.class,
                    validatorConfigJson);

            Map<String, SecretValidatorConfigurationModel> ruleConfigurations = new HashMap<>();
            for (SecretValidatorConfigurationModel configData : configurationDataList.getValidatorConfigList()) {
                ruleConfigurations.put(configData.getRuleId(), configData);
            }
            return ruleConfigurations;
        } catch (Exception e) {
            throw new IllegalStateException("Creating secret validator configuration from: " + validatorConfigFile + " failed!", e);
        }

    }

}
