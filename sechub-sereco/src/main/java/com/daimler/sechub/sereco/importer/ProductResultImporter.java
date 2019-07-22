// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import java.io.IOException;

import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.MetaData;

public interface ProductResultImporter {

	public MetaData importResult(String json) throws IOException;

	public ProductImportAbility isAbleToImportForProduct(ImportParameter param);
}