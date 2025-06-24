// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotation;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotationType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;

public class AbstractProductMetaDataBuilder<T extends AbstractProductMetaDataBuilder<T>> {

    private boolean forSecurityProduct = true;
    ImportParameter parameter;

    @SuppressWarnings("unchecked")
    public T forSecurityProduct(boolean forSecurityProduct) {
        this.forSecurityProduct = forSecurityProduct;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T forParam(ImportParameter param) {
        this.parameter = param;
        return (T) this;
    }

    /**
     * Creates a new SERECO meta data object. If marked as
     * {@link #forSecurityProduct}, the given annoation type will be added
     *
     * @param type
     * @param annotationTextPart - an additional info, will be rendered after
     *                           calculated product information
     * @return meta data, never <code>null</code>
     */
    protected SerecoMetaData createSerecoMetaDataAndAppendAnnotationWhenSecurityProduct(SerecoAnnotationType type, String annotationTextPart) {
        SerecoMetaData data = new SerecoMetaData();

        if (forSecurityProduct) {

            String productId = null;
            if (parameter != null) {
                productId = parameter.getProductId();
            }

            StringBuilder description = new StringBuilder();
            description.append("Security product '");
            description.append(productId);
            description.append("' ");
            description.append(annotationTextPart);
            description.append('.');

            String value = description.toString();

            SerecoAnnotation annotation = new SerecoAnnotation();
            annotation.setValue(value);
            annotation.setType(type);

            data.getAnnotations().add(annotation);

        }

        return data;
    }

}
