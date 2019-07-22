// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.test;

import com.daimler.sechub.test.TestFileSupport;

public class SerecoTestFileSupport extends TestFileSupport{

	public static final String NETSPARKER_RESULT_XML_TESTFILE1= "netsparker/netsparker_v1.0.40.109_scan_result_output_vulnerabilities.xml";
	public static final String NETSPARKER_RESULT_JSON_TESTFILE1= "netsparker/netsparker_v1.0.40.109_scan_result_output_vulnerabilities.json";
	
	public static final SerecoTestFileSupport INSTANCE = new SerecoTestFileSupport();
	
	protected SerecoTestFileSupport() {
		super("sechub-sereco/src/test/resources");
	}

}
