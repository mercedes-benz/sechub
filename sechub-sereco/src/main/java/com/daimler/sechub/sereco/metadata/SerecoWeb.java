package com.daimler.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoWeb {

    SerecoWebRequest request = new SerecoWebRequest();
    SerecoWebResponse response = new SerecoWebResponse();

    public SerecoWebRequest getRequest() {
        return request;
    }

    public SerecoWebResponse getResponse() {
        return response;
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
        SerecoWeb other = (SerecoWeb) obj;
        return Objects.equals(request, other.request) && Objects.equals(response, other.response);
    }

    @Override
    public String toString() {
        return "SerecoWeb [" + (request != null ? "\n>request=" + request + ", " : "") + (response != null ? "\n>response=" + response : "") + "]";
    }

}
