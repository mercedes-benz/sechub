// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotation;
import com.mercedesbenz.sechub.sereco.metadata.SerecoAnnotationType;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

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

        addArtificialVulnerability(descriptionAsString, data);

        /* new way */
        SerecoAnnotation failedMessage = new SerecoAnnotation();
        failedMessage.setValue(descriptionAsString);
        failedMessage.setType(SerecoAnnotationType.INTERNAL_ERROR_PRODUCT_FAILED);

        data.getAnnotations().add(failedMessage);

        return data;
    }

    @Deprecated
    /* deprecated way: we add an artificial vulnerability */
    private void addArtificialVulnerability(String descriptionAsString, SerecoMetaData data) {
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        vulnerability.setSeverity(SerecoSeverity.CRITICAL);
        vulnerability.setType("SecHub failure");
        vulnerability.setDescription(descriptionAsString);

        data.getVulnerabilities().add(vulnerability);
    }

    public ProductFailureMetaDataBuilder forParam(ImportParameter param) {
        this.parameter = param;
        return this;
    }

}
