// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.execution;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final SecretValidatorExecutionContextFactory contextFactory;
    private final SecretValidationService validationService;
    private final SerecoSeveritySarifEnhancementService sarifEnhancementService;
    private final SarifValidationSupport sarifValidationSupport;

    /* @formatter:off */
    public SecretValidatorExecutionService(SecretValidatorExecutionContextFactory contextFactory,
            SecretValidationService validationService,
            SerecoSeveritySarifEnhancementService sarifEnhancementService,
            SarifValidationSupport sarifValidationSupport) {

        this.contextFactory = contextFactory;
        this.validationService = validationService;
        this.sarifEnhancementService = sarifEnhancementService;
        this.sarifValidationSupport = sarifValidationSupport;
        /* @formatter:on */
    }

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
                    validateFindingAndEnhanceSarif(config, finding, executionContext.getMaximumRetries());
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

    private void validateFindingAndEnhanceSarif(SecretValidatorConfigurationModel config, Result finding, int maximumRetries) {
        for (Location location : finding.getLocations()) {
            if (!sarifValidationSupport.findingLocationCanBeValidated(location)) {
                continue;
            }
            Region findingRegion = location.getPhysicalLocation().getRegion();
            SecretValidationResult validationResult = validationService.validateFindingByRegion(findingRegion, config.getRuleId(), config.getRequests(),
                    maximumRetries);
            sarifEnhancementService.addSerecoSeverityInfo(validationResult, findingRegion, config.getCategorization());
        }
    }

}
