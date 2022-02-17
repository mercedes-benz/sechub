// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.config;

/**
 * Just a special variant to have possibility to change ScanConfig
 *
 * @author Albert Tregnaghi
 *
 */
public class DeveloperToolsScanConfigService extends ScanConfigService {

    public void switchConfigurationIfChanged(ScanConfig config) {
        super.switchConfigurationIfChanged(config);
    }

}
