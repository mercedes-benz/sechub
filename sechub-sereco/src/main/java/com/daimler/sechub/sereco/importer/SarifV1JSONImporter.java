// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.contrastsecurity.sarif.CodeFlow;
import com.contrastsecurity.sarif.Location;
import com.contrastsecurity.sarif.PhysicalLocation;
import com.contrastsecurity.sarif.ReportingDescriptor;
import com.contrastsecurity.sarif.ReportingDescriptorReference;
import com.contrastsecurity.sarif.Result;
import com.contrastsecurity.sarif.Result.Level;
import com.contrastsecurity.sarif.Run;
import com.contrastsecurity.sarif.SarifSchema210;
import com.contrastsecurity.sarif.ThreadFlow;
import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.sereco.ImportParameter;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This Importer supports SARIF Version 2.1.0
 * https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html
 */
@Component
public class SarifV1JSONImporter extends AbstractProductResultImporter {

    private ObjectMapper mapper;

    public SarifV1JSONImporter() {
        mapper = new ObjectMapper();
    }

    public SerecoMetaData importResult(String data) throws IOException {

        if (data == null) {
            data = "";
        }

        SarifSchema210 sarif;

        try {
            sarif = readSarifSchema210(data);
        } catch (Exception e) {
            /*
             * here we can throw the exception - should never happen, because with
             * #isAbleToImportForProduct we already check this is possible. So there is
             * something odd here and we throw the exception
             */
            throw new IOException("Import cannot parse sarif 2.1.0 json", e);
        }

        SerecoMetaData metaData = new SerecoMetaData();

        sarif.getRuns().stream().forEach(run -> {

            List<Result> results = resultsWithGroupedLocations(run.getResults());

            results.stream().forEach(result -> {
                SerecoVulnerability vulnerability = new SerecoVulnerability();

                ResultData resultData = resolveData(run, result);
                vulnerability.setType(resultData.shortName);
                vulnerability.setDescription(resultData.description);
                vulnerability.setSeverity(mapToSeverity(result.getLevel()));
                vulnerability.getClassification().setCwe(cweIdFrom(result));

                vulnerability.setCode(resolveCodeInfoFromResult(result));

                // Static Analysis Results Format (SARIF) is only suitable for our CODE_SCAN
                vulnerability.setScanType(ScanType.CODE_SCAN);

                metaData.getVulnerabilities().add(vulnerability);
            });
        });

        return metaData;
    }

    private class ResultData {
        String shortName;
        String description;
    }

    private ResultData resolveData(Run run, Result result) {
        String ruleId = result.getRuleId();

        Set<ReportingDescriptor> rules = run.getTool().getDriver().getRules();
        /* @formatter:off */
        ReportingDescriptor reportingDescription = 
                        rules.
                        stream().
                        filter(r ->r.getId().equals(ruleId)).
                        findFirst().orElse(null);
        /* @formatter:on */

        ResultData data = new ResultData();
        if (reportingDescription != null) {
            data.shortName = resolveShortName(reportingDescription);
            data.description = resolveDescription(reportingDescription);
        }
        if (data.shortName == null) {
            data.shortName = "Undefined"; // at least we set this to indicate there is no name defined inside SARIF
        }

        return data;
    }

    private String resolveDescription(ReportingDescriptor reportingDescription) {
        return reportingDescription.getFullDescription().getText();
    }

    private String resolveShortName(ReportingDescriptor desc) {
        String name = desc == null ? null : desc.getName();
        int index = name.lastIndexOf("/");
        if (index != -1) {
            index++;
            if (index < name.length() - 1) {
                name = name.substring(index);
            }
        }
        return name;
    }

    private SarifSchema210 readSarifSchema210(String data) throws JsonProcessingException, JsonMappingException {
        return mapper.readValue(data, SarifSchema210.class);
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

            SerecoCodeCallStackElement subCode = new SerecoCodeCallStackElement();
            subCode.setLocation(physicalLocation.getArtifactLocation().getUri());
            subCode.setLine(physicalLocation.getRegion().getStartLine());
            subCode.setColumn(physicalLocation.getRegion().getStartColumn());

            callstack.add(subCode);
        });
        return callstack;
    }

    private String cweIdFrom(Result result) {

        Optional<ReportingDescriptorReference> taxa = result.getTaxa().stream().filter(comp -> comp.getToolComponent().getName() == "CWE").findFirst();

        if (!taxa.isPresent()) {
            return "";
        }

        return taxa.get().getId();
    }

    private List<Result> resultsWithGroupedLocations(List<Result> results) {

        Map<Integer, Result> mappedResults = new HashMap<>();

        results.stream().forEach(result -> {
            if (!mappedResults.containsKey(result.getRuleIndex())) {
                mappedResults.put(result.getRuleIndex(), result);
            } else {
                mappedResults.get(result.getRuleIndex()).getLocations().addAll(result.getLocations());
            }

        });

        return new ArrayList<Result>(mappedResults.values());
    }

    private SerecoSeverity mapToSeverity(Level level) {
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
        ProductImportAbility ability = super.isAbleToImportForProduct(param);
        if (ability != ProductImportAbility.ABLE_TO_IMPORT) {
            return ability;
        }
        /* okay it's a json and it contains "runs" */
        try {
            /*
             * Currently the easiest way to identify if this JSON content can be imported is
             * to just try a JSON deserialization here. So we just read Sarif schema. If
             * this is a SARIF file the read will be done twice, but this is something we
             * have to live with. Reading this to a field is no option, because this is a
             * component with standard spring injection scope, so only a singleton and we
             * must be not stateful here.
             */
            readSarifSchema210(param.getImportData());
        } catch (Exception e) {
            return ProductImportAbility.NOT_ABLE_TO_IMPORT;
        }

        return ProductImportAbility.ABLE_TO_IMPORT;
    }

    @Override
    protected ImportSupport createImportSupport() {
        return ImportSupport.builder().productId("PDS_CODESCAN").mustBeJSON().contentIdentifiedBy("\"runs\"").build();
    }

}
