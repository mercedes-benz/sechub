package com.mercedesbenz.sechub.adapter.mock;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

public class NullMockDataIdentifierFactory implements MockDataIdentifierFactory {

    @Override
    public String createMockDataIdentifier(ScanType scanType, SecHubConfigurationModel configuration) {
        /* always null - means no identifier */
        return null;
    }

}
