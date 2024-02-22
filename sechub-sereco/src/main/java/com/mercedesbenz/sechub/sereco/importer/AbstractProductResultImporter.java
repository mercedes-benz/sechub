// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import com.mercedesbenz.sechub.sereco.ImportParameter;

public abstract class AbstractProductResultImporter implements ProductResultImporter {

    private ImportSupport importSupport;

    public AbstractProductResultImporter() {
        this.importSupport = createImportSupport();
    }

    /**
     * Creates an import support which helps to make some fast standard check if
     * import is possible
     *
     * @return import support
     */
    protected abstract ImportSupport createImportSupport();

    protected ImportSupport getImportSupport() {
        return importSupport;
    }

    /**
     * The default implementation does only use the import support to check if
     * import is possible or not. If this is not enough for a 100% check please
     * override this method.<br>
     * <h3>Originally the interface describes:</h3> {@inheritDoc}
     */
    public boolean isAbleToImportForProduct(ImportParameter param) {
        return importSupport.isAbleToImport(param);
    }

}
