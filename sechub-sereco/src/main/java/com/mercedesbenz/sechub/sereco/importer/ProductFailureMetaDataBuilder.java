// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotationType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;

public class ProductFailureMetaDataBuilder extends AbstractProductMetaDataBuilder<ProductFailureMetaDataBuilder> {

    public SerecoMetaData build() {

        return createSerecoMetaDataAndAppendAnnotationWhenSecurityProduct(SerecoAnnotationType.INTERNAL_ERROR_PRODUCT_FAILED,
                "failed, so cannot give a correct answer");
    }

}
