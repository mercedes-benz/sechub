// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import java.util.Optional;

import org.springframework.http.HttpStatus;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.model.SecHubReportMetaData;
import com.mercedesbenz.sechub.commons.model.SecHubRevisionData;
import com.mercedesbenz.sechub.commons.model.SecHubVersionControlData;
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
    private static final int MAX_LOCATION_INFO_STRING_LENTH = 180;

    public static HTMLReportHelper DEFAULT = new HTMLReportHelper();

    public boolean hasDescription(SecHubFinding finding) {
        return SimpleStringUtils.isNotEmpty(getDescription(finding));
    }

    public boolean hasSolution(SecHubFinding finding) {
        return SimpleStringUtils.isNotEmpty(getSolution(finding));
    }

    public String createShortLocationInfo(SecHubFinding finding) {

        if (finding == null) {
            return EMPTY_STRING;
        }
        ScanType scanType = finding.getType();
        if (scanType == null) {
            return EMPTY_STRING;
        }

        String locationInfo = "";
        switch (scanType) {
        case LICENSE_SCAN:
        case SECRET_SCAN:
        case IAC_SCAN:
        case CODE_SCAN:
            SecHubCodeCallStack code = finding.getCode();
            if (code != null) {
                locationInfo = code.getLocation() + ", line:" + code.getLine() + ", column:" + code.getColumn();
            }
            break;
        case INFRA_SCAN:
            locationInfo = finding.getDescription();
            break;
        case WEB_SCAN:
            locationInfo = createShortTargetLocation(finding.getWeb());
            break;
        default:
            break;
        }
        return SimpleStringUtils.truncateWhenTooLong(locationInfo, MAX_LOCATION_INFO_STRING_LENTH);

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
        if (web == null) {
            return EMPTY_STRING;
        }
        String target = getTargetLocation(web);
        int questionMarkIndex = target.indexOf('?');
        if (questionMarkIndex != LINE_NOT_FOUND) {
            target = target.substring(0, questionMarkIndex);
        }
        return target;
    }

    public String getTargetLocation(SecHubReportWeb web) {
        if (web == null) {
            return EMPTY_STRING;
        }
        String target = web.getRequest().getTarget();
        if (target == null) {
            return EMPTY_STRING;
        }
        return target;
    }

    public String createShortEvidence(SecHubReportWebAttack attack) {
        if (attack == null) {
            return EMPTY_STRING;
        }
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

    public String createFindingLink(SecHubFinding finding) {
        return "#" + createFindingAnkerId(finding);
    }

    public String createFindingAnkerId(SecHubFinding finding) {
        if (finding == null) {
            return EMPTY_STRING;
        }
        return "finding_" + finding.getId();
    }

    public String createFirstFindingAnkerId(SecHubFinding finding) {
        return getLinkSupport().createAnkerFirstOf(finding.getType(), finding.getSeverity());
    }

    public String createCweLink(SecHubFinding finding) {
        if (finding == null) {
            return EMPTY_STRING;
        }
        return createCweLink(finding.getCweId());
    }

    public String createCweLink(Integer cweId) {
        if (cweId == null) {
            return EMPTY_STRING;
        }
        return "https://cwe.mitre.org/data/definitions/" + cweId + ".html";
    }

    public String createCweText(SecHubFinding finding) {
        if (finding == null) {
            return EMPTY_STRING;
        }
        return createCweText(finding.getCweId());
    }

    public String createCweText(Integer cweId) {
        if (cweId == null) {
            return EMPTY_STRING;
        }
        return "CWE-" + cweId;
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
        return EMPTY_STRING;
    }

    public SecHubVersionControlData getVersionControl(Optional<SecHubReportMetaData> metaData) {
        return resolveVersionControlOrNull(metaData);
    }

    public boolean hasVersionControl(Optional<SecHubReportMetaData> metaData) {
        return resolveVersionControlOrNull(metaData) != null;
    }

    public boolean hasRevisionData(SecHubFinding finding) {
        return getRevisionData(finding) != null;
    }

    public SecHubRevisionData getRevisionData(SecHubFinding finding) {
        if (finding == null) {
            return null;
        }
        Optional<SecHubRevisionData> revisionOpt = finding.getRevision();
        if (revisionOpt.isEmpty()) {
            return null;
        }
        return revisionOpt.get();
    }

    private SecHubVersionControlData resolveVersionControlOrNull(Optional<SecHubReportMetaData> metaDataOpt) {
        if (metaDataOpt == null) {
            return null;
        }
        if (metaDataOpt.isEmpty()) {
            return null;
        }
        Optional<SecHubVersionControlData> versionControlOpt = metaDataOpt.get().getVersionControl();
        if (versionControlOpt.isEmpty()) {
            return null;
        }
        return versionControlOpt.get();
    }

    private HTMLFirstLinkToSeveritySupport getLinkSupport() {
        return HTMLFirstLinkToSeveritySupport.DEFAULT;
    }

}
