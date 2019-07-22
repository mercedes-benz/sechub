// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

public enum ProductImportAbility {

	/**
	 * The importer is able to import
	 */
	ABLE_TO_IMPORT,

	/**
	 * The importer will not be able to import
	 */
	NOT_ABLE_TO_IMPORT,

	/**
	 * The importer would potentially be able to import, but product failed
	 * (means an empty string as result)
	 */
	PRODUCT_FAILED,


}
