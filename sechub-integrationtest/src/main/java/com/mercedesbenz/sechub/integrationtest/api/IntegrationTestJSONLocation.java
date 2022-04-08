// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

public enum IntegrationTestJSONLocation {

    CLIENT_JSON_SOURCESCAN_GREEN("sechub-integrationtest-client-sourcescan-green.json"),

    CLIENT_JSON_SOURCESCAN_GENERIC_TEMPLATE("sechub-integrationtest-client-sourcescan-generic-template.json"),

    CLIENT_JSON_SOURCESCAN_GENERIC_TEMPLATE_NO_DATA_SECTION("sechub-integrationtest-client-sourcescan-generic-template-no-data-section.json"),

    /** will use checkmarx-mockdata-multiple.xml as result */
    CLIENT_JSON_SOURCESCAN_YELLOW("sechub-integrationtest-client-sourcescan-yellow.json"),

    CLIENT_JSON_SOURCESCAN_EXLUDE_SOME_FILES("sechub-integrationtest-client-sourcescan-excluded_some_files.json"),

    CLIENT_JSON_SOURCESCAN_GREEN_EXTREME_BIG("sechub-integrationtest-client-sourcescan-green-extreme-big.json"),

    CLIENT_JSON_INFRASCAN("sechub-integrationtest-client-infrascan.json"),

    JSON_WEBSCAN_SCENARIO3_PRODUCTFAILURE("sechub-integrationtest-webscanconfig-scenario3-productfailure.json"),

    JSON_WEBSCAN_RED("sechub-integrationtest-webscanconfig-red-result.json");

    private String path;

    private IntegrationTestJSONLocation(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
