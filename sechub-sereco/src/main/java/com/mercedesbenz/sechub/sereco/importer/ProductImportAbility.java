// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

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
     * The importer would potentially be able to import, but product failed or was
     * canceled (in both cases an empty string is used as product result)
     */
    PRODUCT_FAILED_OR_CANCELED,

}
