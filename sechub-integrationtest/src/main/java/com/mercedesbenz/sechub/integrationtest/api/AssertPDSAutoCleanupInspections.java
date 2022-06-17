// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import com.mercedesbenz.sechub.integrationtest.internal.autoclean.AbstractAssertAutoCleanupInspections;

public class AssertPDSAutoCleanupInspections extends AbstractAssertAutoCleanupInspections {

    @Override
    protected TestAutoCleanJsonDeleteCountFetcher createFeatcher() {
        return () -> TestAPI.fetchPDSAutoCleanupInspectionDeleteCounts();
    }

}
