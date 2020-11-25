package com.daimler.sechub.restdoc;

import com.epages.restdocs.apispec.Schema;

enum OpenApiSchema {
    MAPPING_CONFIGURATION("MappingConfiguration"),
    JOB_STATUS("JobStatus");
    
    private final Schema schema;
    
    private OpenApiSchema(String schemaName) {
        schema = new Schema(schemaName);
    }
    
    Schema getSchema() {
        return schema;
    }
}
