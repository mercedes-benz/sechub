// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.metadata;

import java.util.List;

public class MetaDataAccess {

	public static void setClassification(Vulnerability search, Classification classification) {
		search.classification=classification;
	}

	public static Vulnerability createVulnerability(String url, String type, Severity severity, List<Detection> list, String description,
			Classification classification) {
		return new Vulnerability(url, type, severity, list, description, classification);
	}

}
