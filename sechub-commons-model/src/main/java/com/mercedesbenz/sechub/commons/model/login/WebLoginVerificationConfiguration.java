// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.login;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebLoginVerificationConfiguration {

    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_RESPONSE_CODE = "responseCode";
    public static final int DEFAULT_VALUE_RESPONSE_CODE = 200;
    private URL url;
    private int responseCode;

    public WebLoginVerificationConfiguration() {
        this.responseCode = DEFAULT_VALUE_RESPONSE_CODE;
    }

    @JsonProperty(PROPERTY_URL)
    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @JsonProperty(PROPERTY_RESPONSE_CODE)
    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
