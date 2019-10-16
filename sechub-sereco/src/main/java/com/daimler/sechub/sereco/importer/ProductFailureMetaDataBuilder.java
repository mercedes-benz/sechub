// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import com.daimler.sechub.sereco.ImportParameter;
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

		SerecoMetaData data = new SerecoMetaData();

		SerecoVulnerability v = new SerecoVulnerability();
		v.setSeverity(SerecoSeverity.CRITICAL);
		v.setType("SecHub failure");
		v.setDescription(description.toString());

		data.getVulnerabilities().add(v);

		return data;
	}

	public ProductFailureMetaDataBuilder forParam(ImportParameter param) {
		this.parameter=param;
		return this;
	}

}
