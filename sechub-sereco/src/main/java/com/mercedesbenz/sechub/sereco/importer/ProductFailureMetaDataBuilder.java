// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotation;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotationType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;

public class ProductFailureMetaDataBuilder {

    private ImportParameter parameter;

    public SerecoMetaData build() {

        String productId = null;
        if (parameter != null) {
            productId = parameter.getProductId();
        }
        StringBuilder description = new StringBuilder();
        description.append("Security product '");
        description.append(productId);
        description.append("' failed, so cannot give a correct answer.");

        String descriptionAsString = description.toString();
        SerecoMetaData data = new SerecoMetaData();

        /* new way */
        SerecoAnnotation failedMessage = new SerecoAnnotation();
        failedMessage.setValue(descriptionAsString);
        failedMessage.setType(SerecoAnnotationType.INTERNAL_ERROR_PRODUCT_FAILED); // important. Will be used to handle traffic light (OFF)!

        data.getAnnotations().add(failedMessage);

        return data;
    }

    public ProductFailureMetaDataBuilder forParam(ImportParameter param) {
        this.parameter = param;
        return this;
    }

}
