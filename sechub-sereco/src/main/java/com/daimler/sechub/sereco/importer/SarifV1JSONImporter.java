package com.daimler.sechub.sereco.importer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.contrastsecurity.*;
import com.contrastsecurity.sarif.SarifSchema210;
import com.contrastsecurity.sarif.ThreadFlow;
import com.contrastsecurity.sarif.CodeFlow;
import com.contrastsecurity.sarif.PhysicalLocation;
import com.contrastsecurity.sarif.ReportingDescriptorReference;
import com.contrastsecurity.sarif.Result;
import com.contrastsecurity.sarif.Result.Level;
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
            run.getResults().stream().forEach(result -> {
                SerecoVulnerability vulnerability = new SerecoVulnerability();

                vulnerability.setDescription("");
                vulnerability.setSeverity(mapToSeverity(result.getLevel()));
                vulnerability.getClassification().setCwe(cweIdFrom(result));

                vulnerability.setCode(resolveCodeInfoFromResult(result));

            });
        });

        return metaData;
    }

    private SerecoCodeCallStackElement resolveCodeInfoFromResult(Result result) {

        Optional<CodeFlow> codeFlows = result.getCodeFlows().stream().findFirst();
        
        if (!codeFlows.isPresent()) {
            return null;
        }
        
        Optional<ThreadFlow> optFlow = codeFlows.get().getThreadFlows().stream().findFirst();
        
        if (!optFlow.isPresent()) {
            return null;
        }
        
        ThreadFlow flow = optFlow.get();
        
        List<SerecoCodeCallStackElement> callstack = Arrays.asList();
        
        flow.getLocations().forEach(location -> {
            
            PhysicalLocation physicalLocation = location.getLocation().getPhysicalLocation();
            
            SerecoCodeCallStackElement subCode = new SerecoCodeCallStackElement();
            subCode.setLocation(physicalLocation.getArtifactLocation().getUri());
            subCode.setLine(physicalLocation.getRegion().getStartLine());
            subCode.setColumn(physicalLocation.getRegion().getStartColumn());
            
            callstack.add(subCode);
        });
        
        SerecoCodeCallStackElement firstElement = null;
        SerecoCodeCallStackElement prevElement = null;
        
        for (SerecoCodeCallStackElement code : callstack) {
            if (firstElement == null) {
                firstElement = code;
            }
            else if (prevElement == null) {
                firstElement.setCalls(code);
                prevElement = code;
            }
        }
        
        return firstElement;
    }

    private String cweIdFrom(Result result) {

        Optional<ReportingDescriptorReference> taxa = result.getTaxa().stream().filter(comp -> comp.getToolComponent().getName() == "CWE").findFirst();

        if (!taxa.isPresent()) {
            return "";
        }

        return taxa.get().getId();
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
