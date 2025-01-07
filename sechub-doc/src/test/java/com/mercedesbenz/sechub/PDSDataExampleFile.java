// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

public enum PDSDataExampleFile {

    PDS_PARAM_TEMPLATE_META_DATA_SYNTAX("src/docs/asciidoc/documents/shared/snippet/pds-param-template-metadata-syntax.json");
    ;

    private String path;

    private PDSDataExampleFile(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
