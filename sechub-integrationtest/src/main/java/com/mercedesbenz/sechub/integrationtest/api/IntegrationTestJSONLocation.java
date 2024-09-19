// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

/**
 * Enumeration with test json locations. To map the corresponding mock
 * information you can read the referenced JSON file and map wih the content
 * found inside `/sechub-other/mockdata/mockdata_setup.json`
 *
 * @author Albert Tregnaghi
 *
 */
public enum IntegrationTestJSONLocation {

    CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT("sechub-integrationtest-client-sourcescan-green.json"),

    CLIENT_JSON_SOURCESCAN_GREEN_1_SECOND_WAIT("sechub-integrationtest-client-sourcescan-green-1-second.json"),

    CLIENT_JSON_SOURCESCAN_GENERIC_TEMPLATE("sechub-integrationtest-client-sourcescan-generic-template.json"),

    CLIENT_JSON_SOURCESCAN_GENERIC_TEMPLATE_NO_DATA_SECTION("sechub-integrationtest-client-sourcescan-generic-template-no-data-section.json"),

    CLIENT_JSON_SOURCESCAN_YELLOW_ZERO_WAIT("sechub-integrationtest-client-sourcescan-yellow.json"),

    CLIENT_JSON_SOURCESCAN_YELLOW_2_SECONDS_WAIT("sechub-integrationtest-client-sourcescan-yellow-2-seconds.json"),

    CLIENT_JSON_SOURCESCAN_EXLUDE_SOME_FILES("sechub-integrationtest-client-sourcescan-excluded_some_files.json"),

    /**
     * Contains many configuration entries inside JSON, but always sane ebntry:
     * `testProject1/src/java`
     */
    CLIENT_JSON_SOURCESCAN_GREEN_ZERO_WAIT_BIG_CONFIGFILE("sechub-integrationtest-client-sourcescan-green-extreme-big.json"),

    CLIENT_JSON_INFRASCAN("sechub-integrationtest-client-infrascan.json"),

    CLIENT_JSON_WEBSCAN_PRODUCTFAILURE_ZERO_WAIT("sechub-integrationtest-webscanconfig-scenario3-productfailure.json"),

    /**
     * Returns web scan result with one criticial finding.
     */
    CLIENT_JSON_WEBSCAN_RED_ZERO_WAIT("sechub-integrationtest-webscanconfig-red-result.json"),

    /**
     * Returns web scan result with multiple findings: low, medium, high, criticial
     */
    CLIENT_JSON_WEBSCAN_RED_MANYFINDINGS_ZERO_WAIT("sechub-integrationtest-webscanconfig-manyfindings-red-result.json"),

    CLIENT_JSON_SECRET_SCAN_YELLOW_ZERO_WAIT("sechub-integrationtest-secretscanconfig.json"),

    CLIENT_JSON_REMOTE_SCAN_CONFIGURATION("sechub-integrationtest-remote-scan-configuration.json");

    private String path;

    private IntegrationTestJSONLocation(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
