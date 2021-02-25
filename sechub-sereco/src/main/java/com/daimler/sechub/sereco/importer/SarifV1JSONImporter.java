package com.daimler.sechub.sereco.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.contrastsecurity.sarif.SarifSchema210;
import com.contrastsecurity.sarif.ThreadFlow;
import com.contrastsecurity.sarif.CodeFlow;
import com.contrastsecurity.sarif.Location;
import com.contrastsecurity.sarif.PhysicalLocation;
import com.contrastsecurity.sarif.ReportingDescriptorReference;
import com.contrastsecurity.sarif.Result;
import com.contrastsecurity.sarif.Result.Level;
import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoMetaData;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SarifV1JSONImporter extends AbstractProductResultImporter {

    public SerecoMetaData importResult(String data) throws IOException {

        if (data == null) {
            data = "";
        }

        ObjectMapper mapper = new ObjectMapper();
        SarifSchema210 sarif;

        try {
            sarif = mapper.readValue(data, SarifSchema210.class);
        } catch (Exception e) {
            throw new IOException("Import cannot parse sarif 2.1.0 json", e);
        }

        SerecoMetaData metaData = new SerecoMetaData();

        sarif.getRuns().stream().forEach(run -> {
            
            List<Result> results = resultsWithGroupedLocations(run.getResults());
            
            results.stream().forEach(result -> {
                SerecoVulnerability vulnerability = new SerecoVulnerability();

                vulnerability.setDescription("");
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
            }
            else {
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
    protected ImportSupport createImportSupport() {
        return ImportSupport.builder().productId("SARIF").mustBeJSON().build();
    }

}
