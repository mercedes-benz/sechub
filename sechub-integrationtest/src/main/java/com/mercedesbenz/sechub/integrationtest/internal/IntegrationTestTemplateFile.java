// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

public enum IntegrationTestTemplateFile {

    UPDATE_METADATA("sechub-integrationtest-updatemetadata.json"),

    UPDATE_WHITELIST("sechub-integrationtest-updatewhitelist1.json"),

    /**
     * Simple webscan setup with (empty) login setup
     *
     * <h3>Login setup</h3>
     *
     * <pre>
     * <code>
     * {
     *   "login" : {
     *   }
     * }
     * </code>
     * </pre>
     */
    WEBSCAN_1("sechub-integrationtest-webscanconfig1.json"),

    /**
     * Similar to as {@link #WEBSCAN_1} but with login setup + meta data inside:
     * <br>
     * <h3>Login setup</h3>
     *
     * <pre>
     * <code>
     * "login" : {
     *      "templateData" : {
     *          "variables" : {
     *              "mandatory-variable-1" : "value-1"
     *          }
     *      }
     *  }
     * </code>
     * </pre>
     *
     * <h3>Meta data:</h3>
     *
     * <h4>Labels</h4>
     *
     * <pre>
     * stage:testing
     * purpose:quality assurance
     * </pre>
     */
    WEBSCAN_2("sechub-integrationtest-webscanconfig2.json"),

    /**
     * Same as {@link #WEBSCAN_1} but with meta data inside :
     *
     * <h3>Labels</h3>
     *
     * <pre>
     * stage:testing
     * purpose:security assurance
     * reviewer:senior-security-expert-A
     * </pre>
     */
    WEBSCAN_3("sechub-integrationtest-webscanconfig3.json"),

    CODE_SCAN_1_SOURCE_EMBEDDED("sechub-integrationtest-sourcescanconfig1.json"),

    CODE_SCAN_2_BINARIES_DATA_ONE_REFERENCE("sechub-integrationtest-codescan-binaries-config1-one-data-section-only-folder.json"),

    CODE_SCAN_3_SOURCES_DATA_ONE_REFERENCE("sechub-integrationtest-codescan-sources-config1-one-data-section-only-folder.json"),

    ;

    private String templateFilename;

    private IntegrationTestTemplateFile(String templateFileName) {
        this.templateFilename = templateFileName;
    }

    public String getTemplateFilename() {
        return templateFilename;
    }
}
