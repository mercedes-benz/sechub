// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan;

import com.daimler.sechub.domain.scan.project.ScanProjectConfigID;
import com.daimler.sechub.domain.scan.project.ScanProjectMockDataConfiguration;
import com.daimler.sechub.sharedkernel.TypedKey;

/**
 * Contains some typed keys of scan domain
 *
 * @author Albert Tregnaghi
 *
 */
public class ScanKey {

    public static final TypedKey<ScanProjectMockDataConfiguration> PROJECT_MOCKDATA_CONFIGURATION = new ScanProjectMockDataConfigurationKey();

    private static class ScanProjectMockDataConfigurationKey implements TypedKey<ScanProjectMockDataConfiguration> {

        private ScanProjectMockDataConfigurationKey() {
        }

        @Override
        public String getId() {
            return ScanProjectConfigID.MOCK_CONFIGURATION.getId();
        }

        @Override
        public Class<ScanProjectMockDataConfiguration> getValueClass() {
            return ScanProjectMockDataConfiguration.class;
        }

    }

}