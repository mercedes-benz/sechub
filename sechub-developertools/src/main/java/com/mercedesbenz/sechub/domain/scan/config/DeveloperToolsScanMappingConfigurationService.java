// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.config;

/**
 * Just a special variant to have possibility to change ScanMappingConfiguration
 *
 * @author Albert Tregnaghi
 *
 */
public class DeveloperToolsScanMappingConfigurationService extends ScanMappingConfigurationService {

    public void switchConfigurationIfChanged(ScanMappingConfiguration config) {
        super.switchConfigurationIfChanged(config);
    }

}
