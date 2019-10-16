// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.metadata;

import java.util.ArrayList;
import java.util.List;

public class SerecoMetaData {

	private List<SerecoVulnerability> vulnerabilities = new ArrayList<>();

	public List<SerecoVulnerability> getVulnerabilities() {
		return vulnerabilities;
	}

}
