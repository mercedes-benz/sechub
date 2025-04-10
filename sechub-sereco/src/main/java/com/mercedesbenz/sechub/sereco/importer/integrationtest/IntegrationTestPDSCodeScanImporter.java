// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer.integrationtest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Component
@Profile(Profiles.INTEGRATIONTEST) // we provide this importer only at integration tests
/**
 * Please read
 * /sechub-integrationtest/src/test/resources/pds/codescan/upload/README.md for
 * syntax description integration test output - ProductImportAbility.
 *
 * @author Albert Tregnaghi
 *
 */
public class IntegrationTestPDSCodeScanImporter extends AbstractIntegrationTestResultImporter {

    private static final String ID_PDS_INTTEST_PRODUCT_CODESCAN = "#PDS_INTTEST_PRODUCT_CODESCAN";
    private static final String ID_PDS_INTTEST_PRODUCT_CODESCAN_FAILED = "#PDS_INTTEST_PRODUCT_CODESCAN_FAILED";

    @Override
    protected String getImportFailedLineCommentIdentifier() {
        return ID_PDS_INTTEST_PRODUCT_CODESCAN_FAILED;
    }

    @Override
    protected String getImportSuccessLineCommentIdentifier() {
        return ID_PDS_INTTEST_PRODUCT_CODESCAN;
    }

}
