// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoLicenseDocument;
import com.mercedesbenz.sechub.sereco.metadata.SerecoLicenseSpdx;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;

@Component
public class SpdxV1JSONImporter extends AbstractProductResultImporter {
    private static final Logger LOG = LoggerFactory.getLogger(SpdxV1JSONImporter.class);

    @Override
    public SerecoMetaData importResult(String spdxJson, ScanType scanType) throws IOException {
        Objects.requireNonNull(spdxJson, "SPDX cannot be null.");

        SerecoMetaData metaData = new SerecoMetaData();

        if (isValidJson(spdxJson)) {
            try {
                SerecoLicenseSpdx spdxDocument = SerecoLicenseSpdx.of(spdxJson);
                SerecoLicenseDocument licenseDocument = new SerecoLicenseDocument();
                licenseDocument.setSpdx(spdxDocument);
                metaData.getLicenseDocuments().add(licenseDocument);
            } catch (IllegalArgumentException e) {
                LOG.info(e.getMessage());
            }
        }

        return metaData;
    }

    public boolean isValidJson(String spdxJson) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.readTree(spdxJson);
        } catch (JacksonException e) {
            LOG.info("Unable to read SPDX Json.");
            return false;
        }
        return true;
    }

    @Override
    protected ImportSupport createImportSupport() {
        return ImportSupport.builder().mustBeJSON().contentIdentifiedBy(SerecoLicenseSpdx.SPDX_JSON_IDENTIFIER).build();
    }

}
