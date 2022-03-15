package com.mercedesbenz.sechub.docgen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.JSONable;

/**
 * Represents a model for generated "resources.json" by Spring RestDoc
 * (com.epages variant)
 *
 * @author Albert Tregnaghi
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestDocResourceModel implements JSONable<RestDocResourceModel> {

    private static RestDocResourceModel IMPORTER = new RestDocResourceModel();

    public RestDocRequest request;
    public RestDocResponse response;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class RestDocRequest {
        public String path;

        public String method;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class RestDocResponse {
        public int status;
    }

    @Override
    public Class<RestDocResourceModel> getJSONTargetClass() {
        return RestDocResourceModel.class;
    }

    public static RestDocResourceModel fromString(String string) {
        return IMPORTER.fromJSON(string);
    }

}
