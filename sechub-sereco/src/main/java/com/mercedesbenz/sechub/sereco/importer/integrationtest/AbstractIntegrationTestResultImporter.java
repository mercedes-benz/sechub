// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer.integrationtest;

import java.io.IOException;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.importer.ProductResultImporter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

/**
 * Simple test importer for integration tests. Can import a text file if the
 * identifier is found at first line of a text file to import. Inside the text
 * file we got different lines containing: ${severity}:${message}
 *
 * <h3>Example file:</h3> (the identifier is here not listed, shall be part of a
 * comment)
 *
 * <pre>
 * <code>
 * CRITICAL:i am a critical error
 * MEDIUM:i am a medium error
 * LOW:i am just a low error
 * INFO:i am just an information
 * </code>
 * </pre>
 *
 * Severity must be one of the provided SerecoSeverity.java parts
 */
public abstract class AbstractIntegrationTestResultImporter implements ProductResultImporter {

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
            vulnerability.setScanType(scanType);
            vulnerability.setCode(code);

            vulnerability.setSeverity(SerecoSeverity.fromString(severity));

            vulnerabilities.add(vulnerability);
        }

        return metaData;
    }

    @Override
    public boolean isAbleToImportForProduct(ImportParameter param) {

        String data = param.getImportData();
        if (!data.startsWith(getImportSuccessLineCommentIdentifier())) {
            return false;
        }
        if (data.contains(getImportFailedLineCommentIdentifier())) {
            return false;
        }
        return true;
    }

    protected abstract String getImportFailedLineCommentIdentifier();

    protected abstract String getImportSuccessLineCommentIdentifier();

}
