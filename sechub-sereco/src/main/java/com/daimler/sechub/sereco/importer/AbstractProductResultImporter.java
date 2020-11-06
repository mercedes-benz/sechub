// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import com.daimler.sechub.sereco.ImportParameter;

public abstract class AbstractProductResultImporter implements ProductResultImporter{

	private ImportSupport importSupport;

	public AbstractProductResultImporter() {
		this.importSupport=createImportSupport();
	}

	protected abstract ImportSupport createImportSupport();

	protected ImportSupport getImportSupport() {
		return importSupport;
	}

	public final ProductImportAbility isAbleToImportForProduct(ImportParameter param) {
		return importSupport.isAbleToImport(param);
	}
	
	
}
