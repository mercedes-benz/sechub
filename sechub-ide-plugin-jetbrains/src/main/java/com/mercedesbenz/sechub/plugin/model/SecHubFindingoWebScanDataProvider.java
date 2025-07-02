// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.model;

import java.util.Map;

import com.mercedesbenz.sechub.api.internal.gen.model.*;

public class SecHubFindingoWebScanDataProvider {

    public String getWebAttackDescription(SecHubFinding secHubFinding) {
        if (secHubFinding == null) {
            return "No SecHub finding available";
        }
        SecHubReportWeb web = secHubFinding.getWeb();
        if (web == null) {
            return "No SecHub web report data available";
        }
        SecHubReportWebRequest request = web.getRequest();
        if (request == null) {
            return "No web request web data available";
        }

        SecHubReportWebAttack webAttack = web.getAttack();
        if (webAttack == null) {
            return "No web attack data available";
        }
        String attackVector = webAttack.getVector();
        SecHubReportWebEvidence evidence = webAttack.getEvidence();
        String bodyLocationText = "";
        String evidenceSnippet = "";

        if (evidence != null) {
            SecHubReportWebBodyLocation bodyLocation = evidence.getBodyLocation();
            if (bodyLocation != null) {
                bodyLocationText += bodyLocation.getStartLine();
            }
            evidenceSnippet = evidence.getSnippet();

        }
        if (attackVector == null) {
            attackVector = "";
        }
        if (bodyLocationText == null) {
            bodyLocationText = "";
        }
        if (evidenceSnippet == null) {
            evidenceSnippet = "";
        }
        String text = """
                Attack vector: %s

                Body location: %s

                Evidence:
                ------------------------------------------------
                %s


                """.formatted(attackVector, bodyLocationText, evidenceSnippet);
        return text;
    }

    public String getWebRequestDescription(SecHubFinding secHubFinding) {
        if (secHubFinding == null) {
            return "No SecHub finding available";
        }
        SecHubReportWeb web = secHubFinding.getWeb();
        if (web == null) {
            return "No SecHub web report data available";
        }
        SecHubReportWebRequest request = web.getRequest();
        if (request == null) {
            return "No web request web data available";
        }

        String method = request.getMethod();
        String url = request.getTarget();
        StringBuilder headers = new StringBuilder();
        for (Map.Entry<String, Object> entry : request.getHeaders().entrySet()) {
            headers.append(entry.getKey()).append("=").append(entry.getValue());
            headers.append("\n");
        }

        SecHubReportWebBody body = request.getBody();
        String bodyString = null;
        boolean binaryBody = false;
        if (body != null) {
            if (body.getText() != null) {
                bodyString = "Text:\n" + body.getText();
            } else if (body.getBinary() != null) {
                bodyString = "Binary:\n" + body.getBinary();
            } else {
                bodyString = "";
            }
        }

        String text = """
                Method: %s
                URL: %s

                Headers:
                ------------------------------------------------
                %s

                Body:
                ------------------------------------------------
                %s


                """.formatted(method, url, headers.toString(), bodyString);
        return text;
    }

    public String getWebResponseDescription(SecHubFinding secHubFinding) {
        if (secHubFinding == null) {
            return "No SecHub finding available";
        }
        SecHubReportWeb web = secHubFinding.getWeb();
        if (web == null) {
            return "No SecHub web report data available";
        }
        SecHubReportWebResponse response = web.getResponse();
        if (response == null) {
            return "No web response web data available";
        }

        StringBuilder headers = new StringBuilder();
        for (Map.Entry<String, Object> entry : response.getHeaders().entrySet()) {
            headers.append(entry.getKey()).append("=").append(entry.getValue());
            headers.append("\n");
        }

        SecHubReportWebBody body = response.getBody();
        String bodyString = null;
        if (body != null) {
            if (body.getText() != null) {
                bodyString = "Text:\n" + body.getText();
            } else if (body.getBinary() != null) {
                bodyString = "Binary:\n" + body.getBinary();
            }
        }
        if (bodyString == null) {
            bodyString = "";
        }
        String resultInfoText = response.getProtocol() + "/" + response.getVersion() + " " + response.getStatusCode() + " " + response.getReasonPhrase();

        String text = """
                %s

                Headers:
                ------------------------------------------------
                %s

                Body:
                ------------------------------------------------
                %s


                """.formatted(resultInfoText, headers.toString(), bodyString);
        return text;
    }
}
