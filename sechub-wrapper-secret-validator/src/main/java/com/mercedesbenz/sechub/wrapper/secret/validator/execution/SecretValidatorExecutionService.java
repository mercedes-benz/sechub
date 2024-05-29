// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorConfigurationModel;

import de.jcup.sarif_2_1_0.model.Result;
import de.jcup.sarif_2_1_0.model.Run;

@Service
public class SecretValidatorExecutionService {
    private static final Logger LOG = LoggerFactory.getLogger(SecretValidatorExecutionService.class);

    @Autowired
    SecretValidatorExecutionContextFactory contextFactory;

    public void execute() {
        SecretValidatorExecutionContext executionContext = contextFactory.create();
        Map<String, SecretValidatorConfigurationModel> validatorConfiguration = executionContext.getValidatorConfiguration();

        List<Run> runs = executionContext.getSarifReport().getRuns();
        for (Run run : runs) {
            List<Result> results = run.getResults();
            for (Result result : results) {
                SecretValidatorConfigurationModel config = validatorConfiguration.get(result.getRuleId());
                if (config == null) {
                    LOG.info("No config found to validate findings of rule: {}", result.getRuleId());
                    continue;
                }
                // TODO: winzj, 2024-06-13: validate and categorize the findings
                
            }
        }
    }

}
