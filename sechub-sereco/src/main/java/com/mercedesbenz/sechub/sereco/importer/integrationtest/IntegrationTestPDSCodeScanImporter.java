// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer.integrationtest;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.importer.ProductImportAbility;
import com.mercedesbenz.sechub.sereco.importer.ProductResultImporter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
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
public class IntegrationTestPDSCodeScanImporter implements ProductResultImporter {

    private static final String ID_PDS_INTTEST_PRODUCT_CODESCAN = "#PDS_INTTEST_PRODUCT_CODESCAN";
    private static final String ID_PDS_INTTEST_PRODUCT_CODESCAN_FAILED = "#PDS_INTTEST_PRODUCT_CODESCAN_FAILED";

    @Override
    public SerecoMetaData importResult(String simpleText, ScanType scanType) throws IOException {
        String[] lines = simpleText.split("\n");
        SerecoMetaData metaData = new SerecoMetaData();
        List<SerecoVulnerability> vulnerabilities = metaData.getVulnerabilities();
        int pseudoLineNumber = 0;
        for (String line : lines) {
            pseudoLineNumber++; // we just reuse result line...
            if (line.startsWith("#")) {
                continue;
            }

            String[] splitted = line.split(":");
            if (splitted.length < 2) {
                continue;
            }
            int pos = 0;
            String severity = splitted[pos++];
            String message = splitted[pos++];

            SerecoCodeCallStackElement code = new SerecoCodeCallStackElement();
            code.setColumn(123);
            code.setLine(pseudoLineNumber);
            code.setLocation("data.txt");
            code.setRelevantPart("integrationtest");
            code.setSource("integration test code only!");

            SerecoVulnerability vulnerability = new SerecoVulnerability();
            vulnerability.setDescription(message);
            vulnerability.setScanType(ScanType.CODE_SCAN);
            vulnerability.setCode(code);

            vulnerability.setSeverity(SerecoSeverity.fromString(severity));

            vulnerabilities.add(vulnerability);
        }

        return metaData;
    }

    @Override
    public ProductImportAbility isAbleToImportForProduct(ImportParameter param) {

        String data = param.getImportData();
        if (!data.startsWith(ID_PDS_INTTEST_PRODUCT_CODESCAN)) {
            return ProductImportAbility.NOT_ABLE_TO_IMPORT;
        }
        if (data.contains(ID_PDS_INTTEST_PRODUCT_CODESCAN_FAILED)) {
            return ProductImportAbility.PRODUCT_FAILED_OR_CANCELED;
        }
        return ProductImportAbility.ABLE_TO_IMPORT;
    }

}
