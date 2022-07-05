// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

/**
 * Special product identifiers used for PDS integration tests. <br>
 * <br>
 * <br>
 * For all PDS integrations tests, we only use ONE running PDS server which
 * provides multiple products via these identifiers. The PDS server
 * configurations file location is:<br>
 * <br>
 * <b>
 *
 * <code>
 * sechub-integration/src/main/resources/pds-config-integrationtest.json
 * </code>
 *
 * </b> <br>
 * <br>
 * Inside this configuration file you can see details about parameters etc.
 *
 * @author Albert Tregnaghi
 *
 */
/*
 * For the id of a product identifier only 30 characters are allowed - please
 * keep this in mind, when adding new identifiers.
 */
public enum PDSIntTestProductIdentifier {
    /**
     * PDS product identifier for integration test PDS script/server which will
     * return simplified text lines containing fake/test findings for code scans.
     * <br>
     * <br>
     * For details please look at {@link PDSIntTestProductIdentifier}.
     */
    PDS_INTTEST_CODESCAN("PDS_INTTEST_PRODUCT_CODESCAN"),

    /**
     * PDS product identifier for integration test PDS script/server which will
     * return simplified text lines containing fake/test findings for infra scans.
     * <br>
     * <br>
     * For details please look at {@link PDSIntTestProductIdentifier}.
     */
    PDS_INTTEST_INFRASCAN("PDS_INTTEST_PRODUCT_INFRASCAN"),

    /**
     * PDS product identifier for integration test PDS script/server which will
     * return simplified text lines containing fake/test findings for web scans.
     * <br>
     * <br>
     * For details please look at {@link PDSIntTestProductIdentifier}.
     */
    PDS_INTTEST_WEBSCAN("PDS_INTTEST_PRODUCT_WEBSCAN"),

    /**
     * PDS product identifier for integration test PDS script/server which will
     * return SARIF json for code scans. <br>
     * <br>
     * For details please look at {@link PDSIntTestProductIdentifier}.
     */
    PDS_INTTEST_PRODUCT_CS_SARIF("PDS_INTTEST_PRODUCT_CS_SARIF"),

    /**
     * PDS product identifier for integration test PDS script/server which will
     * return OWASP ZAP based SARIF json for web scans. The used script will just
     * return content from existing test output files. <br>
     * <br>
     * For details please look at {@link PDSIntTestProductIdentifier}.
     */
    PDS_INTTEST_PRODUCT_WS_SARIF("PDS_INTTEST_PRODUCT_WS_SARIF"),

    /**
     * PDS product identifier for PDS license scan integration test. The PDS license
     * scan script will return an already existing SPDX JSON file as output. <br>
     * For details please look at {@link PDSIntTestProductIdentifier}.
     */
    PDS_TEST_PRODUCT_LICENSESCAN("PDS_TEST_PRODUCT_LICENSESCAN"),

    ;

    private String id;

    private PDSIntTestProductIdentifier(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
