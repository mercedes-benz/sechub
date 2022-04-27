// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.io.IOException;

import com.mercedesbenz.sechub.sereco.metadata.SerecoLicenseDocument;
import com.mercedesbenz.sechub.sereco.metadata.SerecoLicenseSpdx;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;

public class SpdxV1JSONImporter extends AbstractProductResultImporter {

    @Override
    public SerecoMetaData importResult(String spdx) throws IOException {
        SerecoMetaData metaData = new SerecoMetaData();

        if (spdx != null && !spdx.isEmpty()) {
            SerecoLicenseSpdx spdxDocument = SerecoLicenseSpdx.of(spdx);
            SerecoLicenseDocument licenseDocument = new SerecoLicenseDocument();
            licenseDocument.setSpdx(spdxDocument);
            metaData.getLicenseDocuments().add(licenseDocument);
        }

        return metaData;
    }

    @Override
    protected ImportSupport createImportSupport() {
        return ImportSupport.builder().mustBeJSON().contentIdentifiedBy("\"SPDXID\" : \"SPDXRef-DOCUMENT\"").build();
    }

}
