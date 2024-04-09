// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test.report;

import com.mercedesbenz.sechub.sereco.importer.SarifImportProductWorkaroundSupport;
import com.mercedesbenz.sechub.sereco.importer.SarifV1JSONImporter;

/**
 * This importer provides an initialized SarifImportProductWorkaroundSupport,
 * because we do not want to use Spring Boot dependencies inside 'sechub-test'
 */
public class TestSarifV1JSONImporter extends SarifV1JSONImporter {

    public TestSarifV1JSONImporter() {
        this.workaroundSupport = new SarifImportProductWorkaroundSupport();
    }

}
