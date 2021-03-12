// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import java.util.List;

import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.daimler.sechub.sharedkernel.validation.AbstractValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationContext;

/**
 * This validation validates only the standard/minimum configuration parts which
 * are always necessary for any kind of Checkmarx communication. 
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
public class CheckmarxProductExecutorMinimumConfigValidation extends AbstractValidation<ProductExecutorConfig> {

    public CheckmarxProductExecutorMinimumConfigValidation() {
    }

    @Override
    protected String getValidatorName() {
        return "Checkmarx executor config validation";
    }

    @Override
    protected void setup(AbstractValidation<ProductExecutorConfig>.ValidationConfig config) {

    }

    @Override
    protected void validate(ValidationContext<ProductExecutorConfig> context) {
        /* check not null... */
        validateNotNull(context);
        ProductExecutorConfig configToValidate = getObjectToValidate(context);
        if (configToValidate == null) {
            return;
        }
        ProductExecutorConfigSetup setup = configToValidate.getSetup();
        validateNotNull(context, setup, "setup");

        if (setup == null) {
            return;
        }

        List<ProductExecutorConfigSetupJobParameter> jobParameters = setup.getJobParameters();
        validateNotNull(context, jobParameters, "jobParameters");
        if (jobParameters == null) {
            return;
        }

        validateMandatoryPartsSet(context, jobParameters);
    }

    private void validateMandatoryPartsSet(ValidationContext<ProductExecutorConfig> context, List<ProductExecutorConfigSetupJobParameter> jobParameters) {
        /* check mandatory fields are set */

        // currently we have no mandatory parts - with #470 when we replace the ENV
        // parts from CheckmarxInstallSetup with runtime config, we must check this here
    }

}
