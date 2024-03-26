// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer.integrationtest;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.importer.ProductResultImporter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sharedkernel.Profiles;

@Component
@Profile(Profiles.INTEGRATIONTEST) // we provide this importer only at integration tests
/**
 * Please read /sechub-integrationtest/src/test/resources/pds/webcan/README.md
 * for syntax description integration test output - ProductImportAbility.
 *
 * @author Albert Tregnaghi
 *
 */
public class IntegrationTestPDSCWebScanImporter implements ProductResultImporter {

    private static final String ID_PDS_INTTEST_PRODUCT_WEBSCAN = "#PDS_INTTEST_PRODUCT_WEBSCAN";
    private static final String ID_PDS_INTTEST_PRODUCT_WEBSCAN_FAILED = "#PDS_INTTEST_PRODUCT_WEBSCAN_FAILED";

    @Override
    public SerecoMetaData importResult(String simpleText, ScanType scanType) throws IOException {
        String[] lines = simpleText.split("\n");
        SerecoMetaData metaData = new SerecoMetaData();
        List<SerecoVulnerability> vulnerabilities = metaData.getVulnerabilities();
        for (String line : lines) {
            if (line.startsWith("#")) {
                continue;
            }

            String[] splitted = line.split(":");
            if (splitted.length < 2) {
                continue;
            }
            int pos = 0;
            String severity = splitted[pos++];
            String message = line.substring(severity.length() + 1);// we use the full other content here, so we can have https://xyz.example.com as
                                                                   // message content!

            SerecoVulnerability vulnerability = new SerecoVulnerability();
            vulnerability.setDescription(message);
            vulnerability.setScanType(ScanType.WEB_SCAN);

            vulnerability.setSeverity(SerecoSeverity.fromString(severity));

            vulnerabilities.add(vulnerability);
        }

        return metaData;
    }

    @Override
    public boolean isAbleToImportForProduct(ImportParameter param) {

        String data = param.getImportData();
        if (!data.startsWith(ID_PDS_INTTEST_PRODUCT_WEBSCAN)) {
            return false;
        }
        if (data.contains(ID_PDS_INTTEST_PRODUCT_WEBSCAN_FAILED)) {
            return false;
        }
        return true;
    }

}
