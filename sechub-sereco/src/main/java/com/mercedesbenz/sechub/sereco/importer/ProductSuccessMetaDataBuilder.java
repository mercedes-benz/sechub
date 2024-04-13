// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotationType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;

public class ProductSuccessMetaDataBuilder extends AbstractProductMetaDataBuilder<ProductSuccessMetaDataBuilder> {

    public SerecoMetaData build() {

        return createSerecoMetaDataAndAppendAnnotationWhenSecurityProduct(SerecoAnnotationType.INTERNAL_INFO_PRODUCT_SUCCESSFUL_IMPORTED,
                "successfully executed and import was possible");
    }
}
