// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan;

import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.TrafficLight;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWeb;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebAttack;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebBody;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebBodyLocation;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebEvidence;
import com.mercedesbenz.sechub.commons.model.web.SecHubReportWebResponse;

public class HTMLReportHelper {

    private static final int LINE_NOT_FOUND = -1;
    private static final String EMPTY_STRING = "";
    private static final int SHORT_VECTOR_SIZE = 80;
    private static final int SHORT_EVIDENCE_SIZE = 80;

    public static HTMLReportHelper DEFAULT = new HTMLReportHelper();

    public String createSummaryTableAnkerLinkForRed(ScanType scanType) {
        return createSummaryTableAnkerId(TrafficLight.RED, scanType);
    }

    public String createSummaryTableAnkerLinkForYellow(ScanType scanType) {
        return createSummaryTableAnkerId(TrafficLight.YELLOW, scanType);
    }

    public String createSummaryTableAnkerLinkForGreen(ScanType scanType) {
        return createSummaryTableAnkerId(TrafficLight.GREEN, scanType);
    }

    String createSummaryTableAnkerId(TrafficLight trafficLight, ScanType scanType) {
        String trafficLightPrefix = "unknown";
        String scanTypeName = "NoScanType";

        if (trafficLight != null) {
            trafficLightPrefix = trafficLight.name().toLowerCase();
        }

        if (scanType != null) {
            scanTypeName = scanType.getId();
            scanTypeName = scanTypeName.substring(0, 1).toUpperCase() + scanTypeName.substring(1);
        }

        return trafficLightPrefix + scanTypeName + "Table";
    }

    public boolean hasDescription(SecHubFinding finding) {
        return SimpleStringUtils.isNotEmpty(getDescription(finding));
    }

    public boolean hasSolution(SecHubFinding finding) {
        return SimpleStringUtils.isNotEmpty(getSolution(finding));
    }

    public String getDescription(SecHubFinding finding) {
        if (finding == null) {
            return EMPTY_STRING;
        }
        return finding.getDescription();
    }

    public String getSolution(SecHubFinding finding) {
        if (finding == null) {
            return EMPTY_STRING;
        }
        return finding.getSolution();
    }

    public boolean hasEvidenceStartLine(SecHubReportWebAttack attack) {
        return getEvidenceStartLine(attack) >= 0;
    }

    public int getEvidenceStartLine(SecHubReportWebAttack attack) {
        if (attack == null) {
            return LINE_NOT_FOUND;
        }
        SecHubReportWebEvidence evidence = attack.getEvidence();
        if (evidence == null) {
            return LINE_NOT_FOUND;
        }
        SecHubReportWebBodyLocation bodyLocation = evidence.getBodyLocation();
        if (bodyLocation == null) {
            return LINE_NOT_FOUND;
        }
        return bodyLocation.getStartLine();

    }

    public String createStatusAndResponseDescription(SecHubReportWebResponse response) {
        if (response == null) {
            return EMPTY_STRING;
        }
        int statusCode = response.getStatusCode();

        StringBuilder sb = new StringBuilder();
        sb.append(statusCode);
        sb.append(" ");
        try {
            sb.append(HttpStatus.valueOf(statusCode).name());
        } catch (IllegalArgumentException e) {
            sb.append("unknown statuscode");
        }
        if (SimpleStringUtils.isNotEmpty(response.getReasonPhrase())) {
            sb.append(" - ");
            sb.append(response.getReasonPhrase());
        }
        return sb.toString();
    }

    public boolean hasNoBodyContent(SecHubReportWebBody body) {
        if (body == null) {
            return true;
        }
        if (SimpleStringUtils.isEmpty(body.getBinary()) && SimpleStringUtils.isEmpty(body.getText())) {
            return true;
        }
        return false;
    }

    public boolean hasAttackVectorContent(SecHubReportWebAttack attack) {
        return SimpleStringUtils.isNotEmpty(getVector(attack));
    }

    public boolean hasEvidenceContent(SecHubReportWebAttack attack) {
        return SimpleStringUtils.isNotEmpty(getEvidence(attack));
    }

    public String createShortTargetLocation(SecHubReportWeb web) {
        String target = getTargetLocation(web);
        int questionMarkIndex = target.indexOf('?');
        if (questionMarkIndex != LINE_NOT_FOUND) {
            target = target.substring(0, questionMarkIndex);
        }
        return target;
    }

    public String getTargetLocation(SecHubReportWeb web) {
        String target = web.getRequest().getTarget();
        if (target == null) {
            return EMPTY_STRING;
        }
        return target;
    }

    public String createShortEvidence(SecHubReportWebAttack attack) {
        String snippet = getEvidence(attack);
        return SimpleStringUtils.truncateWhenTooLong(snippet, SHORT_EVIDENCE_SIZE);
    }

    public String getEvidence(SecHubReportWebAttack attack) {
        if (attack == null) {
            return EMPTY_STRING;
        }
        SecHubReportWebEvidence evidence = attack.getEvidence();
        if (evidence == null) {
            return EMPTY_STRING;
        }
        return evidence.getSnippet();
    }

    public String createShortVector(SecHubReportWebAttack attack) {
        return SimpleStringUtils.truncateWhenTooLong(getVector(attack), SHORT_VECTOR_SIZE);
    }

    public String getVector(SecHubReportWebAttack attack) {
        if (attack == null) {
            return EMPTY_STRING;
        }
        return attack.getVector();
    }

    public String getMessageTypeAsHTMLIcon(SecHubMessageType type) {
        if (type != null) {

            switch (type) {
            case ERROR:
                return "&#128711;"; // probibit
            case INFO:
                return "&#128712;"; // circle
            case WARNING:
                return "&#9888;"; // attention
            }
        }
        /* fallback always "no icon" */
        return "";
    }

}
