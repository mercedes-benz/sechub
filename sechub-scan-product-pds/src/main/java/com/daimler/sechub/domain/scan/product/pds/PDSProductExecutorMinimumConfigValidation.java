// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.pds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetup;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfigSetupJobParameter;
import com.daimler.sechub.sharedkernel.validation.AbstractValidation;
import com.daimler.sechub.sharedkernel.validation.ValidationContext;

/**
 * This validation validates only the standard/minimum configuration parts which
 * are always necessary for any kind PDS communication. Mandatory fields defined
 * by PDS server are NOT tested here.
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
public class PDSProductExecutorMinimumConfigValidation extends AbstractValidation<ProductExecutorConfig> {

    private List<PDSSecHubConfigDataKeyProvider<?>> dataKeyProviders;

    public PDSProductExecutorMinimumConfigValidation() {

        /* setup providers */
        dataKeyProviders = new ArrayList<>();
        dataKeyProviders.addAll(Arrays.asList(PDSProductExecutorKeyProvider.values()));
        dataKeyProviders.addAll(Arrays.asList(PDSConfigDataKeyProvider.values()));
    }

    @Override
    protected String getValidatorName() {
        return "PDS executor config validation";
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
        for (PDSSecHubConfigDataKeyProvider<?> provider : dataKeyProviders) {
            PDSSecHubConfigDataKey<?> key = provider.getKey();
            if (!key.isMandatory()) {
                continue;
            }
            ProductExecutorConfigSetupJobParameter found = null;
            for (ProductExecutorConfigSetupJobParameter parameter : jobParameters) {
                if (key.getId().equals(parameter.getKey())) {
                    found = parameter;
                    break;
                }
            }
            boolean notSet = false;
            if (found == null) {
                notSet = true;
            } else {
                String value = found.getValue();
                if (value == null) {
                    notSet = true;
                } else {
                    notSet = value.trim().isEmpty();
                }
            }
            if (notSet) {
                addErrorMessage(context, "Mandatory field:" + key.getId() + " not set.");
            }
        }
    }

}
