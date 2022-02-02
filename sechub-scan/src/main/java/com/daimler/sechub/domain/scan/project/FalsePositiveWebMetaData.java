// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import java.util.Objects;

public class FalsePositiveWebMetaData {

    private FalsePositiveWebRequestMetaData request = new FalsePositiveWebRequestMetaData();
    private FalsePositiveWebResponseMetaData response = new FalsePositiveWebResponseMetaData();

    public FalsePositiveWebRequestMetaData getRequest() {
        return request;
    }

    public FalsePositiveWebResponseMetaData getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "FalsePositiveWebMetaData [" + (request != null ? "request=" + request + ", " : "") + (response != null ? "response=" + response : "") + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(request, response);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FalsePositiveWebMetaData other = (FalsePositiveWebMetaData) obj;
        return Objects.equals(request, other.request) && Objects.equals(response, other.response);
    }
    
    
}
