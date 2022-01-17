package com.daimler.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoWeb {

    SerecoWebRequest request = new SerecoWebRequest();
    SerecoWebResponse response = new SerecoWebResponse();
    SerecoWebAttack attack = new SerecoWebAttack();

    public SerecoWebRequest getRequest() {
        return request;
    }

    public SerecoWebResponse getResponse() {
        return response;
    }
    
    public SerecoWebAttack getAttack() {
        return attack;
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
        /* @formatter:off */
        return "SerecoWeb [" 
                + (attack != null ? "\n"
                + ">attack=" + attack + ", " : "") 
        
                + (request != null ? "\n"
                + ">request=" + request + ", " : "") 
                
                + (response != null ? "\n"
                + ">response=" + response : "") + "]";
        /* @formatter:on */
    }

}
