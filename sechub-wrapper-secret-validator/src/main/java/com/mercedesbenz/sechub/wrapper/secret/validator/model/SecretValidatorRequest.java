// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.secret.validator.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretValidatorRequest {

    private URL url;
    private boolean proxyRequired;
    private List<SecretValidatorRequestHeader> headers = new ArrayList<>();
    private SecretValidatorResponse expectedResponse;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public boolean isProxyRequired() {
        return proxyRequired;
    }

    public void setProxyRequired(boolean proxyRequired) {
        this.proxyRequired = proxyRequired;
    }

    public List<SecretValidatorRequestHeader> getHeaders() {
        return Collections.unmodifiableList(headers);
    }

    public void setHeaders(List<SecretValidatorRequestHeader> headers) {
        if (headers != null) {
            this.headers = headers;
        }
    }

    public SecretValidatorResponse getExpectedResponse() {
        return expectedResponse;
    }

    public void setExpectedResponse(SecretValidatorResponse expectedResponse) {
        this.expectedResponse = expectedResponse;
    }

}
