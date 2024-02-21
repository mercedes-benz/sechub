// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.io.IOException;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;

/**
 * A product result importer is used by SERECO to import / convert product
 * results into SERECO format.
 *
 * @author Albert Tregnaghi
 *
 */
public interface ProductResultImporter {

    /**
     * Start the import. This method is only called when
     * {@link #isAbleToImportForProduct(ImportParameter)} returns
     * {@link ProductImportAbility#ABLE_TO_IMPORT}
     *
     * @param data
     * @param ScanType scanType
     * @return SERECO meta data, never <code>null</code>
     * @throws IOException if this import fails.
     */
    public SerecoMetaData importResult(String data, ScanType scanType) throws IOException;

    /**
     * Checks if the importer is able to import data by given parameters. If
     * {@link ProductImportAbility#ABLE_TO_IMPORT} is returned, the importer is 100%
     * able to import the product result!
     *
     * @param param
     * @return import ability.
     */
    public ProductImportAbility isAbleToImportForProduct(ImportParameter param);

    /**
     * @return name of this import - normally only the simple class name
     */
    public default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Returns <code>true</code> if this importer is for a security product result
     * or not. For example: A preparation does not contain findings but only user
     * messages. In this case the method should return false.
     *
     * @return <code>true</code> when the import is for security product, means
     *         findings, otherwise <code>false</code>
     */
    public default boolean isForSecurityProduct() {
        return true;
    }

}