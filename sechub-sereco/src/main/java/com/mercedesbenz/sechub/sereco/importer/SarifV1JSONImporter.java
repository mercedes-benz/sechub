// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoRevisionData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVersionControl;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebAttack;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebBody;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebBodyLocation;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebEvidence;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebRequest;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebResponse;

import de.jcup.sarif_2_1_0.SarifSchema210ImportExportSupport;
import de.jcup.sarif_2_1_0.SarifSchema210LogicSupport;
import de.jcup.sarif_2_1_0.model.ArtifactContent;
import de.jcup.sarif_2_1_0.model.ArtifactLocation;
import de.jcup.sarif_2_1_0.model.CodeFlow;
import de.jcup.sarif_2_1_0.model.Location;
import de.jcup.sarif_2_1_0.model.Message;
import de.jcup.sarif_2_1_0.model.MultiformatMessageString;
import de.jcup.sarif_2_1_0.model.PhysicalLocation;
import de.jcup.sarif_2_1_0.model.PropertyBag;
import de.jcup.sarif_2_1_0.model.Region;
import de.jcup.sarif_2_1_0.model.ReportingDescriptor;
import de.jcup.sarif_2_1_0.model.ReportingDescriptorReference;
import de.jcup.sarif_2_1_0.model.ReportingDescriptorRelationship;
import de.jcup.sarif_2_1_0.model.Result;
import de.jcup.sarif_2_1_0.model.Result.Level;
import de.jcup.sarif_2_1_0.model.Run;
import de.jcup.sarif_2_1_0.model.SarifSchema210;
import de.jcup.sarif_2_1_0.model.ThreadFlow;
import de.jcup.sarif_2_1_0.model.ToolComponentReference;
import de.jcup.sarif_2_1_0.model.VersionControlDetails;
import de.jcup.sarif_2_1_0.model.WebRequest;
import de.jcup.sarif_2_1_0.model.WebResponse;

/**
 * This Importer supports SARIF
 * https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html
 */
@Component
public class SarifV1JSONImporter extends AbstractProductResultImporter {

    private static final String CWE_ID_798_USE_OF_HARDCODED_CREDENTIALS = "798";

    private static final String CWE = "CWE";

    private static final Logger LOG = LoggerFactory.getLogger(SarifV1JSONImporter.class);

    SarifSchema210ImportExportSupport sarifSchema210ImportExportSupport;
    SarifSchema210LogicSupport sarifSchema210LogicSupport;

    @Autowired
    protected SarifImportProductWorkaroundSupport workaroundSupport;

    public SarifV1JSONImporter() {
        sarifSchema210ImportExportSupport = new SarifSchema210ImportExportSupport();
        sarifSchema210LogicSupport = new SarifSchema210LogicSupport();
    }

    public SerecoMetaData importResult(String data, ScanType scanType) throws IOException {

        if (data == null) {
            data = "";
        }

        SarifSchema210 sarifReport = null;

        try {
            sarifReport = sarifSchema210ImportExportSupport.fromJSON(data);
        } catch (Exception e) {
            /*
             * here we can throw the exception - should never happen, because with
             * #isAbleToImportForProduct we already check this is possible. So there is
             * something odd here and we throw the exception
             */
            throw new IOException("Import cannot parse sarif json", e);
        }
        SerecoMetaData metaData = new SerecoMetaData();

        for (Run run : sarifReport.getRuns()) {
            handleEachRun(run, metaData, scanType);
        }
        return metaData;
    }

    private void handleEachRun(Run run, SerecoMetaData metaData, ScanType scanType) {
        extractFirstVersionControlEntry(run, metaData);

        List<Result> results = run.getResults();

        for (Result result : results) {

            SerecoVulnerability vulnerability = createSerecoVulnerability(run, result, scanType);
            if (vulnerability != null) {
                metaData.getVulnerabilities().add(vulnerability);
            }
        }
    }

    private void extractFirstVersionControlEntry(Run run, SerecoMetaData metaData) {
        SerecoVersionControl existingVersionControl = metaData.getVersionControl();
        if (existingVersionControl != null) {
            return;
        }

        Set<VersionControlDetails> vcp = run.getVersionControlProvenance();
        if (vcp.isEmpty()) {
            return;
        }

        VersionControlDetails firstSarifRunVersionControl = vcp.iterator().next();
        SerecoVersionControl serecoVersionControl = new SerecoVersionControl();

        serecoVersionControl.setRevisionId(firstSarifRunVersionControl.getRevisionId());
        URI repositoryUri = firstSarifRunVersionControl.getRepositoryUri();
        if (repositoryUri != null) {
            serecoVersionControl.setLocation(repositoryUri.toString());
        }
        metaData.setVersionControl(serecoVersionControl);
    }

    private SerecoVulnerability createSerecoVulnerability(Run run, Result result, ScanType scanType) {
        if (result == null) {
            return null;
        }
        SerecoVulnerability vulnerability = new SerecoVulnerability();

        ReportingDescriptor ruleFound = sarifSchema210LogicSupport.fetchRuleForResult(result, run);

        ResultData resultData = resolveData(ruleFound, run, result);
        vulnerability.setType(resultData.identifiedType);
        vulnerability.setDescription(resultData.description);
        vulnerability.setSolution(resultData.solution);

        vulnerability.setSeverity(resolveSeverity(result, run));
        vulnerability.getClassification().setCwe(resolveCweId(scanType, resultData));
        vulnerability.setScanType(scanType);

        String revisionId = workaroundSupport.resolveFindingRevisionId(result, run);
        if (revisionId != null) {
            SerecoRevisionData revision = new SerecoRevisionData();
            revision.setId(revisionId);
            vulnerability.setRevision(revision);
        }

        setWebInformationOrCodeFlow(result, vulnerability);

        return vulnerability;
    }

    private String resolveCweId(ScanType scanType, ResultData data) {
        String cweId = data.cweId;

        if (cweId == null || cweId.isEmpty()) {
            if (scanType == ScanType.SECRET_SCAN) {
                cweId = CWE_ID_798_USE_OF_HARDCODED_CREDENTIALS;
            }
        }
        return cweId;
    }

    private void setWebInformationOrCodeFlow(Result result, SerecoVulnerability vulnerability) {

        WebRequest sarifWebRequest = result.getWebRequest();
        if (sarifWebRequest != null && vulnerability.getScanType() == ScanType.WEB_SCAN) {
            vulnerability.setWeb(resolveWebInfoFromResult(result));
        } else {
            vulnerability.setCode(resolveCodeInfoFromResult(result));
        }
    }

    private SerecoWeb resolveWebInfoFromResult(Result result) {
        SerecoWeb serecoWeb = new SerecoWeb();

        handleWebRequest(result, serecoWeb);
        handleWebResponse(result, serecoWeb);
        handleWebAttack(result, serecoWeb);

        return serecoWeb;

    }

    private void handleWebAttack(Result result, SerecoWeb serecoWeb) {

        List<Location> sarifLocations = result.getLocations();
        if (sarifLocations.size() <= 0) {
            return;
        }

        Location sarifLocation = sarifLocations.iterator().next();
        PhysicalLocation sarifPhysicalLocation = sarifLocation.getPhysicalLocation();
        if (sarifPhysicalLocation == null) {
            return;
        }
        Region sarifRegion = sarifPhysicalLocation.getRegion();
        if (sarifRegion == null) {
            return;
        }

        /* evidence */
        SerecoWebEvidence serecoWebEvidence = new SerecoWebEvidence();
        SerecoWebBodyLocation bodyLocation = new SerecoWebBodyLocation();
        bodyLocation.setStartLine(sarifRegion.getStartLine());
        serecoWebEvidence.setBodyLocation(bodyLocation);

        ArtifactContent sarifSnippet = sarifRegion.getSnippet();
        if (sarifSnippet != null) {
            serecoWebEvidence.setSnippet(sarifSnippet.getText());
        }

        /* attack */
        SerecoWebAttack serecoAttack = serecoWeb.getAttack();

        PropertyBag locationProperties = sarifLocation.getProperties();
        if (locationProperties != null) {
            Object attack = locationProperties.getAdditionalProperties().get("attack");
            if (SimpleStringUtils.isNotEmpty(attack)) {
                serecoAttack.setVector(attack.toString());
            }
        }

        serecoAttack.setEvidence(serecoWebEvidence);

    }

    private void handleWebResponse(Result result, SerecoWeb serecoWeb) {
        SerecoWebResponse serecoReponse = serecoWeb.getResponse();

        WebResponse sarifWebResponse = result.getWebResponse();
        serecoReponse.setProtocol(sarifWebResponse.getProtocol());
        serecoReponse.setVersion(sarifWebResponse.getVersion());
        serecoReponse.setReasonPhrase(sarifWebResponse.getReasonPhrase());
        serecoReponse.setStatusCode(sarifWebResponse.getStatusCode());
        serecoReponse.setNoResponseReceived(sarifWebResponse.getNoResponseReceived());

        serecoReponse.getHeaders().putAll(sarifWebResponse.getHeaders().getAdditionalProperties());

        /* body */
        SerecoWebBody serecoWebResponseBody = serecoReponse.getBody();
        ArtifactContent sarifWebResponseBody = sarifWebResponse.getBody();

        serecoWebResponseBody.setText(sarifWebResponseBody.getText());
        serecoWebResponseBody.setBinary(sarifWebResponseBody.getBinary());
    }

    private void handleWebRequest(Result result, SerecoWeb serecoWeb) {
        SerecoWebRequest serecoWebRequest = serecoWeb.getRequest();

        WebRequest sarifWebRequest = result.getWebRequest();
        serecoWebRequest.setProtocol(sarifWebRequest.getProtocol());
        serecoWebRequest.setVersion(sarifWebRequest.getVersion());
        serecoWebRequest.setMethod(sarifWebRequest.getMethod());
        serecoWebRequest.setTarget(sarifWebRequest.getTarget());
        serecoWebRequest.getHeaders().putAll(sarifWebRequest.getHeaders().getAdditionalProperties());

        /* body */
        SerecoWebBody serecoWebRequestBody = serecoWebRequest.getBody();
        ArtifactContent sarifWebRequestBody = sarifWebRequest.getBody();

        serecoWebRequestBody.setText(sarifWebRequestBody.getText());
        serecoWebRequestBody.setBinary(sarifWebRequestBody.getBinary());
    }

    private SerecoSeverity resolveSeverity(Result result, Run run) {
        SerecoSeverity serecoSeverity = workaroundSupport.resolveCustomSechubSeverity(result, run);
        if (serecoSeverity == null) {
            Level level = sarifSchema210LogicSupport.resolveLevel(result, run);
            return mapToSeverity(level);
        }
        return serecoSeverity;
    }

    private class ResultData {
        String identifiedType;
        String description;
        String cweId;
        String solution;
    }

    private ResultData resolveData(ReportingDescriptor rule, Run run, Result result) {

        ResultData data = new ResultData();
        data.description = resolveMessageTextOrNull(result);

        if (rule != null) {
            data.identifiedType = resolveType(rule, run);
            if (data.description == null) {
                /*
                 * fallback to rule description - not very significant but better than nothing
                 */
                data.description = resolveDescription(rule);
            }
            resolveTargetInformation(rule, data, run);
            resolveSolution(rule, data, run);
        }
        if (data.identifiedType == null) {
            data.identifiedType = "Undefined"; // at least we set this to indicate there is no name defined inside SARIF
        }

        return data;
    }

    private String resolveMessageTextOrNull(Result result) {
        if (result == null) {
            return null;
        }
        Message message = result.getMessage();
        if (message == null) {
            return null;
        }
        return message.getText();
    }

    private void resolveSolution(ReportingDescriptor rule, ResultData data, Run run) {
        PropertyBag ruleProperties = rule.getProperties();
        if (ruleProperties == null) {
            return;
        }
        Object solution = ruleProperties.getAdditionalProperties().get("solution");
        if (!(solution instanceof Map)) {
            return;
        }
        Map<?, ?> solutionAsMap = (Map<?, ?>) solution;
        Object solutionText = solutionAsMap.get("text");
        if (solutionText == null) {
            return;
        }
        data.solution = solutionText.toString();
    }

    private void resolveTargetInformation(ReportingDescriptor rule, ResultData data, Run run) {
        Set<ReportingDescriptorRelationship> relationShips = rule.getRelationships();
        for (ReportingDescriptorRelationship relationShip : relationShips) {
            ReportingDescriptorReference target = relationShip.getTarget();
            if (target == null) {
                continue;
            }
            String id = target.getId();
            if (id == null) {
                continue;
            }
            ToolComponentReference toolComponent = target.getToolComponent();
            if (toolComponent == null) {
                continue;
            }
            String toolComponentName = toolComponent.getName();
            if (toolComponentName == null) {
                continue;
            }

            if (CWE.equalsIgnoreCase(toolComponentName)) {
                /* CWE found, so lets look after the id */
                data.cweId = id;
            }
        }
    }

    private String resolveDescription(ReportingDescriptor rule) {
        return rule.getFullDescription().getText();
    }

    private String resolveType(ReportingDescriptor rule, Run run) {
        if (rule == null) {
            return "error:rule==null!";
        }

        String type = workaroundSupport.resolveType(rule, run);

        if (type == null) {
            MultiformatMessageString shortDescription = rule.getShortDescription();
            if (shortDescription != null) {
                type = shortDescription.getText();
            }
        }

        if (type == null) {
            /*
             * no type identifier found, so do fallback to id, we do not use "name" because
             * this is for i18n!
             */
            type = rule.getId();
        }
        return type;
    }

    private SerecoCodeCallStackElement resolveCodeInfoFromResult(Result result) {
        boolean firstElementfromResultLocation = false;

        SerecoCodeCallStackElement firstElement = resolveCodeInfoFromCodeFlow(result);
        if (firstElement == null) {
            // if no CodeFlow available, try to extract callstack directly from locations
            firstElement = resolveCodeInfoFromLocations(result.getLocations());
            firstElementfromResultLocation = true;
        }
        if (!firstElementfromResultLocation && firstElement != null) {
            /* check source is set at least at first element */
            String source = firstElement.getSource();
            if (source == null || source.trim().isEmpty()) {
                /* not set - last fallback to location */
                SerecoCodeCallStackElement fallbackElement = resolveCodeInfoFromLocations(result.getLocations());
                source = fallbackElement.getSource();
                firstElement.setSource(source);
            }
        }
        return firstElement;
    }

    private SerecoCodeCallStackElement resolveCodeInfoFromCodeFlow(Result result) {

        Optional<CodeFlow> codeFlows = result.getCodeFlows().stream().findFirst();
        if (!codeFlows.isPresent()) {
            return null;
        }

        Optional<ThreadFlow> optFlow = codeFlows.get().getThreadFlows().stream().findFirst();

        if (!optFlow.isPresent()) {
            return null;
        }

        ThreadFlow flow = optFlow.get();

        List<Location> locations = flow.getLocations().stream().map(location -> location.getLocation()).collect(Collectors.toList());

        return resolveCodeInfoFromLocations(locations);
    }

    private SerecoCodeCallStackElement resolveCodeInfoFromLocations(List<Location> locations) {

        List<SerecoCodeCallStackElement> callstack = callStackListFromLocations(locations);

        SerecoCodeCallStackElement firstElement = null;
        SerecoCodeCallStackElement prevElement = null;

        for (SerecoCodeCallStackElement code : callstack) {
            if (firstElement == null) {
                firstElement = code;
            } else if (prevElement == null) {
                firstElement.setCalls(code);
                prevElement = code;
            } else {
                prevElement.setCalls(code);
                prevElement = code;
            }
        }

        return firstElement;
    }

    private List<SerecoCodeCallStackElement> callStackListFromLocations(List<Location> locations) {

        List<SerecoCodeCallStackElement> callstack = new ArrayList<>();

        if (locations == null) {
            return callstack;
        }

        locations.forEach(location -> {

            PhysicalLocation physicalLocation = location.getPhysicalLocation();
            if (physicalLocation != null) {

                SerecoCodeCallStackElement subCode = new SerecoCodeCallStackElement();
                ArtifactLocation artifactLocation = physicalLocation.getArtifactLocation();
                if (artifactLocation != null) {
                    subCode.setLocation(artifactLocation.getUri());

                }
                Region region = physicalLocation.getRegion();
                if (region != null) {
                    subCode.setLine(region.getStartLine());
                    subCode.setColumn(region.getStartColumn());

                    ArtifactContent snippet = region.getSnippet();
                    if (snippet != null) {
                        String text = snippet.getText();
                        if (text != null) {
                            text = text.trim();
                        }
                        subCode.setSource(text);
                    }
                }

                callstack.add(subCode);
            }
        });
        return callstack;
    }

    private SerecoSeverity mapToSeverity(Level level) {
        if (level == null) {
            return SerecoSeverity.UNCLASSIFIED;
        }
        switch (level) {
        case NONE:
        case NOTE:
            return SerecoSeverity.LOW;
        case WARNING:
            return SerecoSeverity.MEDIUM;
        case ERROR:
            return SerecoSeverity.HIGH;
        default:
            return SerecoSeverity.UNCLASSIFIED;
        }
    }

    @Override
    public boolean isAbleToImportForProduct(ImportParameter param) {
        /* first we do the simple check... */
        if (!super.isAbleToImportForProduct(param)) {
            return false;
        }
        boolean validSarif = isValidSarif(param.getImportData());
        if (!validSarif) {
            LOG.warn("Simple check accepted data, but was not valid SARIF");
        }
        return true;
    }

    @Override
    protected ImportSupport createImportSupport() {
        return ImportSupport.builder().mustBeJSON().contentIdentifiedBy("\"runs\"").build();
    }

    private boolean isValidSarif(String json) {
        try {
            sarifSchema210ImportExportSupport.fromJSON(json);
            return true;
        } catch (Exception e) {
            /* ignore error - except for tracing */
            LOG.trace("Not accepted as JSON - " + e.getMessage());
        }
        return false;
    }

}
