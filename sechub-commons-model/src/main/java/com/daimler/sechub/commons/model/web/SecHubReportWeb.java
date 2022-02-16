package com.daimler.sechub.commons.model.web;

import java.util.Objects;

public class SecHubReportWeb {

    private SecHubReportWebRequest request = new SecHubReportWebRequest();
    private SecHubReportWebResponse response = new SecHubReportWebResponse();
    private SecHubReportWebAttack attack = new SecHubReportWebAttack();

    /**
     * @return web request, never <code>null</code>
     */
    public SecHubReportWebRequest getRequest() {
        return request;
    }

    /**
     * @return web response, never <code>null</code>
     */
    public SecHubReportWebResponse getResponse() {
        return response;
    }

    /**
     * @return web attack, never <code>null</code>
     */
    public SecHubReportWebAttack getAttack() {
        return attack;
    }

    @Override
    public int hashCode() {
        return Objects.hash(request, response, attack);
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
        SecHubReportWeb other = (SecHubReportWeb) obj;
        return Objects.equals(request, other.request) && Objects.equals(response, other.response) && Objects.equals(attack, other.attack);
    }

    @Override
    public String toString() {
        /* @formatter:off */
        return "SecHubReportWeb ["
                + (attack != null ? "\n"
                + ">attack=" + attack + ", " : "")

                + (request != null ? "\n"
                + ">request=" + request + ", " : "")

                + (response != null ? "\n"
                + ">response=" + response : "") + "]";
        /* @formatter:on */
    }

}
