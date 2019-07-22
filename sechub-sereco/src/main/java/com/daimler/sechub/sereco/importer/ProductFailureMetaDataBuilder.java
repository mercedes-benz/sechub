// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.MetaData;
import com.daimler.sechub.sereco.metadata.Severity;
import com.daimler.sechub.sereco.metadata.Vulnerability;

public class ProductFailureMetaDataBuilder {

	private ImportParameter parameter;

	public MetaData build() {

		String productId = null;
		if (parameter!=null) {
			productId = parameter.getProductId();
		}
		StringBuilder description = new StringBuilder();
		description.append("Security product '");
		description.append(productId);
		description.append("' failed, so cannot give a correct answer.");

		MetaData data = new MetaData();

		Vulnerability v = new Vulnerability();
		v.setSeverity(Severity.CRITICAL);
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
