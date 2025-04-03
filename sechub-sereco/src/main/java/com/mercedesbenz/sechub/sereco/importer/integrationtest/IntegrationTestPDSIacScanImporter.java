// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer.integrationtest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Component
@Profile(Profiles.INTEGRATIONTEST) // we provide this importer only at integration tests
public class IntegrationTestPDSIacScanImporter extends AbstractIntegrationTestResultImporter {

    private static final String ID_PDS_INTTEST_PRODUCT_IACSCAN = "#PDS_INTTEST_PRODUCT_IACSCAN";
    private static final String ID_PDS_INTTEST_PRODUCT_IACSCAN_FAILED = "#PDS_INTTEST_PRODUCT_IACSCAN_FAILED";

    @Override
    protected String getImportFailedLineCommentIdentifier() {
        return ID_PDS_INTTEST_PRODUCT_IACSCAN_FAILED;
    }

    @Override
    protected String getImportSuccessLineCommentIdentifier() {
        return ID_PDS_INTTEST_PRODUCT_IACSCAN;
    }

}
