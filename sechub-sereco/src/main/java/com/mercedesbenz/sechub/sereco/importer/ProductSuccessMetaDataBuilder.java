// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotation;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotationType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;

public class ProductSuccessMetaDataBuilder {

    private ImportParameter parameter;

    public SerecoMetaData build() {

        String productId = null;
        if (parameter != null) {
            productId = parameter.getProductId();
        }
        StringBuilder description = new StringBuilder();
        description.append("Security product '");
        description.append(productId);
        description.append("' successfully executed and import was possible.");

        String descriptionAsString = description.toString();
        SerecoMetaData data = new SerecoMetaData();

        /* new way */
        SerecoAnnotation scuccessMessage = new SerecoAnnotation();
        scuccessMessage.setValue(descriptionAsString);
        scuccessMessage.setType(SerecoAnnotationType.INTERNAL_INFO_PRODUCT_SUCCESSFUL_IMPORTED); // important. Will be used to handle traffic light (OFF)!

        data.getAnnotations().add(scuccessMessage);

        return data;
    }

    public ProductSuccessMetaDataBuilder forParam(ImportParameter param) {
        this.parameter = param;
        return this;
    }

}
