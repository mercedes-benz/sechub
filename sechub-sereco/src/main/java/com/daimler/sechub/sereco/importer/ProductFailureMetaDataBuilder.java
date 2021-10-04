// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.SerecoAnnotation;
import com.daimler.sechub.sereco.metadata.SerecoAnnotationType;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

public class ProductFailureMetaDataBuilder {

	private ImportParameter parameter;

	public SerecoMetaData build() {

		String productId = null;
		if (parameter!=null) {
			productId = parameter.getProductId();
		}
		StringBuilder description = new StringBuilder();
		description.append("Security product '");
		description.append(productId);
		description.append("' failed, so cannot give a correct answer.");

		String descriptionAsString = description.toString();
		SerecoMetaData data = new SerecoMetaData();

		/* deprecated way: we add an artifical vulnerability */
		SerecoVulnerability v = new SerecoVulnerability();
		v.setSeverity(SerecoSeverity.CRITICAL);
		v.setType("SecHub failure");
        v.setDescription(descriptionAsString);

		data.getVulnerabilities().add(v);
		
		/* new way */
		SerecoAnnotation failedMessage = new SerecoAnnotation();
		failedMessage.setValue(descriptionAsString);
		failedMessage.setType(SerecoAnnotationType.INTERNAL_ERROR_PRODUCT_FAILED);
		
		data.getAnnotations().add(failedMessage);
		
		return data;
	}

	public ProductFailureMetaDataBuilder forParam(ImportParameter param) {
		this.parameter=param;
		return this;
	}

}
