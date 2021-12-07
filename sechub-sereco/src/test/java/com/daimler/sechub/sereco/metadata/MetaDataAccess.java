// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.metadata;

import java.util.List;

public class MetaDataAccess {

	public static void setClassification(SerecoVulnerability search, SerecoClassification classification) {
		search.classification=classification;
	}

	public static void setWebRequest(SerecoVulnerability search, SerecoWebRequest webRequest) {
        search.web.request=webRequest;
    }
	
	public static void setWebResponse(SerecoVulnerability search, SerecoWebResponse webResponse) {
        search.web.response=webResponse;
    }
	
	public static SerecoVulnerability createVulnerability(String url, String type, SerecoSeverity severity, List<SerecoDetection> list, String description,
			SerecoClassification classification) {
		return new SerecoVulnerability(url, type, severity, list, description, classification);
	}

}
