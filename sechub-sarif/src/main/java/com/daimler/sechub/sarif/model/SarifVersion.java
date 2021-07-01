// SPDX-License-Identifier: MIT
package com.daimler.sechub.sarif.model;

public enum SarifVersion {
    // see http://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html
    VERSION_2_1_0("2.1.0", "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json"),

    // see http://docs.oasis-open.org/sarif/sarif/v2.0/sarif-v2.0.html
    VERSION_2_0("2.0", "https://github.com/oasis-tcs/sarif-spec/blob/master/Schemata/sarif-2.0.0-csd.2.beta.2019-01-24.json"),

    ;

    private String version;
    private String schema;

    private SarifVersion(String version, String schema) {
        this.version = version;
        this.schema = schema;
    }

    public String getVersion() {
        return version;
    }

    public String getSchema() {
        return schema;
    }
}
