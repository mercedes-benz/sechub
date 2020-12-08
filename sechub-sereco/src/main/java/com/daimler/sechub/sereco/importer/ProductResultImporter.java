// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import java.io.IOException;

import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;

public interface ProductResultImporter {

	public SerecoMetaData importResult(String data) throws IOException;

	public ProductImportAbility isAbleToImportForProduct(ImportParameter param);
	
    public default String getName() {
        return getClass().getSimpleName();
    }
}