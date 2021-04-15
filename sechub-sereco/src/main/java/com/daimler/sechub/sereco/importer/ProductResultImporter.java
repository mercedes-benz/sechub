// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import java.io.IOException;

import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;

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
     * @return SERECO meta data, never <code>null</code>
     * @throws IOException if this import fails.
     */
    public SerecoMetaData importResult(String data) throws IOException;

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
}