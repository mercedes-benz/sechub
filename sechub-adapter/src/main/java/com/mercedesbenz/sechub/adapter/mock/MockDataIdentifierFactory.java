package com.mercedesbenz.sechub.adapter.mock;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

public interface MockDataIdentifierFactory {

    String createMockDataIdentifier(ScanType scanType, SecHubConfigurationModel configurationModel);

}
