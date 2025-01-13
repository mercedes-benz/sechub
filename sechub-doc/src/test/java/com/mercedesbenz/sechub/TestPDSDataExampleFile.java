// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

public enum TestPDSDataExampleFile {

    PDS_PARAM_TEMPLATE_META_DATA_SYNTAX("src/docs/asciidoc/documents/shared/snippet/pds-param-template-metadata-syntax.json");
    ;

    private String path;

    private TestPDSDataExampleFile(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
