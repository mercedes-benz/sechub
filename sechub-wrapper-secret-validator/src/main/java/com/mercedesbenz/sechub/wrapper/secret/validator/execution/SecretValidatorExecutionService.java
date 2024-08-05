// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorCategorization;
import com.mercedesbenz.sechub.wrapper.secret.validator.model.SecretValidatorConfigurationModel;
import com.mercedesbenz.sechub.wrapper.secret.validator.support.SarifValidationSupport;

import de.jcup.sarif_2_1_0.model.Location;
import de.jcup.sarif_2_1_0.model.Region;
import de.jcup.sarif_2_1_0.model.Result;
import de.jcup.sarif_2_1_0.model.Run;
import de.jcup.sarif_2_1_0.model.SarifSchema210;

@Service
public class SecretValidatorExecutionService {
    private static final Logger LOG = LoggerFactory.getLogger(SecretValidatorExecutionService.class);

    @Autowired
    SecretValidatorExecutionContextFactory contextFactory;

    @Autowired
    SecretValidationService validationService;

    @Autowired
    SerecoSeveritySarifEnhancementService sarifEnhancementService;

    @Autowired
    SarifValidationSupport sarifValidationSupport;

    public SarifSchema210 execute() {
        SecretValidatorExecutionContext executionContext = contextFactory.create();
        Map<String, SecretValidatorConfigurationModel> validatorConfiguration = executionContext.getValidatorConfiguration();

        // generally for secret scans it is only one run
        List<Run> runs = executionContext.getSarifReport().getRuns();
        for (Run run : runs) {
            List<Result> findings = run.getResults();
            for (Result finding : findings) {
                SecretValidatorConfigurationModel config = validatorConfiguration.get(finding.getRuleId());
                if (isValidationPossible(config, finding)) {
                    validateFindingAndEnhanceSarif(executionContext, config, finding);
                }
            }
        }
        return executionContext.getSarifReport();
    }

    private boolean isValidationPossible(SecretValidatorConfigurationModel config, Result finding) {
        if (!sarifValidationSupport.findingCanBeValidated(finding)) {
            return false;
        }
        if (config == null) {
            LOG.info("No config found to validate findings of rule: {}", finding.getRuleId());
            return false;
        }
        SecretValidatorCategorization categorization = config.getCategorization();
        if (categorization == null || categorization.isEmpty()) {
            LOG.info("No config found to categorize findings of rule: {}", finding.getRuleId());
            return false;
        }
        return true;
    }

    private void validateFindingAndEnhanceSarif(SecretValidatorExecutionContext executionContext, SecretValidatorConfigurationModel config, Result finding) {
        for (Location location : finding.getLocations()) {
            if (!sarifValidationSupport.findingLocationCanBeValidated(location)) {
                continue;
            }
            Region findingRegion = location.getPhysicalLocation().getRegion();
            SecretValidationResult validationResult = validationService.validateFindingByRegion(findingRegion, config.getRequests(),
                    executionContext.isTrustAllCertificates());
            sarifEnhancementService.addSerecoSeverityInfo(validationResult, findingRegion, config.getCategorization());
        }
    }

}
