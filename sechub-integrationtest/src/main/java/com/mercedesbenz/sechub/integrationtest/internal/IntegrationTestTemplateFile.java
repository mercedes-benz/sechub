package com.mercedesbenz.sechub.integrationtest.internal;

public enum IntegrationTestTemplateFile {

    UPDATE_METADATA("sechub-integrationtest-updatemetadata.json"),

    UPDATE_WHITELIST("sechub-integrationtest-updatewhitelist1.json"),

    WEBSCAN_1("sechub-integrationtest-webscanconfig1.json"),

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
