// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.sarif.SarifReportSupport;
import com.daimler.sechub.sarif.model.ArtifactLocation;
import com.daimler.sechub.sarif.model.CodeFlow;
import com.daimler.sechub.sarif.model.Level;
import com.daimler.sechub.sarif.model.Location;
import com.daimler.sechub.sarif.model.Message;
import com.daimler.sechub.sarif.model.PhysicalLocation;
import com.daimler.sechub.sarif.model.Region;
import com.daimler.sechub.sarif.model.Report;
import com.daimler.sechub.sarif.model.ReportingDescriptorReference;
import com.daimler.sechub.sarif.model.ReportingDescriptorRelationship;
import com.daimler.sechub.sarif.model.Result;
import com.daimler.sechub.sarif.model.Rule;
import com.daimler.sechub.sarif.model.Run;
import com.daimler.sechub.sarif.model.ThreadFlow;
import com.daimler.sechub.sarif.model.ToolComponentReference;
import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

/**
 * This Importer supports SARIF
 * https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html
 */
@Component
public class SarifV1JSONImporter extends AbstractProductResultImporter {

    private static final String CWE = "CWE";

    private static final Logger LOG = LoggerFactory.getLogger(SarifV1JSONImporter.class);

    SarifReportSupport sarifSupport;

    public SarifV1JSONImporter() {
        sarifSupport = new SarifReportSupport();
    }

    public SerecoMetaData importResult(String data) throws IOException {

        if (data == null) {
            data = "";
        }

        Report report = null;

        try {
            report = sarifSupport.loadReport(data);
        } catch (Exception e) {
            /*
             * here we can throw the exception - should never happen, because with
             * #isAbleToImportForProduct we already check this is possible. So there is
             * something odd here and we throw the exception
             */
            throw new IOException("Import cannot parse sarif json", e);
        }
        SerecoMetaData metaData = new SerecoMetaData();

        for (Run run : report.getRuns()) {
            handleEachRun(run, metaData);
        }
        return metaData;
    }

    private void handleEachRun(Run run, SerecoMetaData metaData) {
        List<Result> results = run.getResults();

        for (Result result : results) {

            SerecoVulnerability vulnerability = createSerecoVulnerability(run, result);
            if (vulnerability != null) {
                metaData.getVulnerabilities().add(vulnerability);
            }
        }
    }

    private SerecoVulnerability createSerecoVulnerability(Run run, Result result) {
        if (result == null) {
            return null;
        }
        SerecoVulnerability vulnerability = new SerecoVulnerability();

        Rule ruleFound = sarifSupport.fetchRuleForResult(result, run);

        ResultData resultData = resolveData(ruleFound, run);
        vulnerability.setType(resultData.identifiedType);
        vulnerability.setDescription(resultData.description);

        vulnerability.setSeverity(resolveSeverity(result, run));
        vulnerability.getClassification().setCwe(resultData.cweId);

        vulnerability.setCode(resolveCodeInfoFromResult(result));

        // Static Analysis Results Format (SARIF) is only suitable for type CODE_SCAN
        vulnerability.setScanType(ScanType.CODE_SCAN);

        return vulnerability;
    }

    private SerecoSeverity resolveSeverity(Result result, Run run) {
        Level level = sarifSupport.resolveLevel(result, run);
        return mapToSeverity(level);
    }

    private class ResultData {
        String identifiedType;
        String description;
        String cweId;
    }

    private ResultData resolveData(Rule rule, Run run) {

        ResultData data = new ResultData();
        if (rule != null) {
            data.identifiedType = resolveType(rule, run);
            data.description = resolveDescription(rule);

            resolveTargetInformation(rule, data, run);
        }
        if (data.identifiedType == null) {
            data.identifiedType = "Undefined"; // at least we set this to indicate there is no name defined inside SARIF
        }

        return data;
    }

    private void resolveTargetInformation(Rule rule, ResultData data, Run run) {
        List<ReportingDescriptorRelationship> relationShips = rule.getRelationships();
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
                /* cwe found, so lets look after the id */
                data.cweId = id;
            }
        }
    }

    private String resolveDescription(Rule rule) {
        return rule.getFullDescription().getText();
    }

    private String resolveType(Rule rule, Run run) {
        if (rule == null) {
            return "error:rule==null!";
        }
        String type = null;
        Message shortDescription = rule.getShortDescription();
        if (shortDescription != null) {
            type = shortDescription.getText();
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

        Optional<CodeFlow> codeFlows = result.getCodeFlows().stream().findFirst();

        if (!codeFlows.isPresent()) {
            // if no CodeFlow available, try to extract callstack directly from locations
            return resolveCodeInfoFromLocations(result.getLocations());
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
    public ProductImportAbility isAbleToImportForProduct(ImportParameter param) {
        /* first we do the simple check... */
        ProductImportAbility ability = super.isAbleToImportForProduct(param);
        if (ability != ProductImportAbility.ABLE_TO_IMPORT) {
            return ability;
        }
        /* okay, now test if its valid SARIF */
        if (sarifSupport.isValidSarif(param.getImportData())) {
            return ProductImportAbility.ABLE_TO_IMPORT;
        }
        LOG.debug("Simple check accepted data, but was not valid SARIF");
        return ProductImportAbility.NOT_ABLE_TO_IMPORT;
    }

    @Override
    protected ImportSupport createImportSupport() {
        return ImportSupport.builder().mustBeJSON().contentIdentifiedBy("\"runs\"").build();
    }

}
