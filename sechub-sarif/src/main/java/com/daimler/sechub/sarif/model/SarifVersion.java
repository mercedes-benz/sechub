package com.daimler.sechub.sarif.model;

public enum SarifVersion {
    VERSION_210("2.1.0", "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json"),
    
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
