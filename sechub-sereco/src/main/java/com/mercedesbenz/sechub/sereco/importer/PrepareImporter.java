// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.prepare.PrepareConstants;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;

/**
 * Special imported for prepare phase. Here we do not import any findings, but
 * we provide the possibility to import defined user messages for reporting.
 *
 * The keyword for a sucessful preparation is {@link #SECHUB_PREPARE_DONE} :
 * {@value #SECHUB_PREPARE_DONE} inside the result text file of a PDS job result
 * (or a direct variant if there is any).
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class PrepareImporter extends AbstractProductResultImporter {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareImporter.class);

    @Override
    public SerecoMetaData importResult(String prepareResultString, ScanType scanType) throws IOException {
        Objects.requireNonNull(prepareResultString, "prepareResultString cannot be null.");

        SerecoMetaData metaData = new SerecoMetaData();
        // just keep empty
        LOG.debug("import empty meta data");

        return metaData;
    }

    @Override
    protected ImportSupport createImportSupport() {
        return ImportSupport.builder().contentIdentifiedBy(PrepareConstants.SECHUB_PREPARE_RESULT).build();
    }

    @Override
    public boolean isForSecurityProduct() {
        // preparation is NOT for a security product
        return false;
    }

}
