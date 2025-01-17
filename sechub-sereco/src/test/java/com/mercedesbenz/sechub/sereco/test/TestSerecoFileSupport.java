// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.test;

import com.mercedesbenz.sechub.test.TestFileSupport;

public class TestSerecoFileSupport extends TestFileSupport {

    public static final String NETSPARKER_RESULT_XML_TESTFILE1 = "netsparker/netsparker_v1.0.40.109_scan_result_output_vulnerabilities.xml";
    public static final String NETSPARKER_RESULT_JSON_TESTFILE1 = "netsparker/netsparker_v1.0.40.109_scan_result_output_vulnerabilities.json";

    public static final String NETSPARKER_V1_9_1_977_XML_TESTFILE = "netsparker/netsparker_v1.9.1.977_scan_result_output_vulnerabilities.xml";

    public static final TestSerecoFileSupport INSTANCE = new TestSerecoFileSupport();

    protected TestSerecoFileSupport() {
        super("sechub-sereco/src/test/resources");
    }

}
