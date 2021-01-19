// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.checkmarx;

import com.daimler.sechub.adapter.DefaultExecutorConfigSupport;
import com.daimler.sechub.domain.scan.product.config.ProductExecutorConfig;
import com.daimler.sechub.sharedkernel.SystemEnvironment;
import com.daimler.sechub.sharedkernel.error.NotAcceptableException;
import com.daimler.sechub.sharedkernel.validation.Validation;

public class CheckmarxExecutorConfigSuppport extends DefaultExecutorConfigSupport {

    /**
     * Creates the configuration support and VALIDATE. This will fail when
     * configuration data is not valid (e.g. mandatory keys missing)
     * 
     * @param config
     * @param systemEnvironment
     * @return support
     * @throws NotAcceptableException when configuration is not valid
     */
    public static CheckmarxExecutorConfigSuppport createSupportAndAssertConfigValid(ProductExecutorConfig config, SystemEnvironment systemEnvironment) {
        return new CheckmarxExecutorConfigSuppport(config, systemEnvironment, new CheckmarxProductExecutorMinimumConfigValidation());
    }

    private CheckmarxExecutorConfigSuppport(ProductExecutorConfig config, SystemEnvironment systemEnvironment, Validation<ProductExecutorConfig> validation) {
        super(config, systemEnvironment, validation);
    }

    public boolean isAlwaysFullScanEnabled() {
        return getParameterBooleanValue(CheckmarxExecutorConfigParameterKeys.CHECKMARX_FULLSCAN_ALWAYS);
    }

}
