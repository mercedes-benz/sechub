// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanProductData;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanResult;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

@Component
public class GenericInfrascanResultImporter extends AbstractProductResultImporter {
    private static final Logger LOG = LoggerFactory.getLogger(GenericInfrascanResultImporter.class);
    private static final Pattern NAME_PATTERN = Pattern.compile("_");
    private static final String GENERIC_CWE_ID = "3105";

    public SerecoMetaData importResult(String json, ScanType scanType) throws IOException {
        if (json == null) {
            json = "";
        }
        
        GenericInfrascanResult result = null;
        try {
            result = JSONConverter.get().fromJSON(GenericInfrascanResult.class, json);
        } catch (JSONConverterException e) {
            throw new IOException("Import cannot parse json", e);
        }
        
        SerecoMetaData metaData = new SerecoMetaData();
        if (!GenericInfrascanResult.ID_GENERIC_INFRASCAN_RESULT.equals(result.getType())){
            LOG.warn("The json did not contain expected type, means cannot import!");
            return metaData;
        }
        
        for (GenericInfrascanProductData productData: result.getProducts()) {
            List<GenericInfrascanFinding> findings = productData.getFindings();
            for (GenericInfrascanFinding finding: findings) {
                String name = finding.getName();
                if (name==null) {
                    name = "";
                }
                SerecoVulnerability vulnerability = new SerecoVulnerability();
                Severity findingSeverity = finding.getSeverity();
                if (findingSeverity==null) {
                    findingSeverity=Severity.UNCLASSIFIED;
                }
                SerecoSeverity severity = SerecoSeverity.fromString(findingSeverity.name());
                vulnerability.setSeverity(severity);
                vulnerability.setType(name);
                vulnerability.setDescription(finding.getDescription());
                vulnerability.setScanType(ScanType.INFRA_SCAN);
                vulnerability.getClassification().setCwe(finding.getCweId() == null ? GENERIC_CWE_ID : String.valueOf(finding.getCweId()));
                vulnerability.getClassification().setCve(finding.getCveId());
                metaData.getVulnerabilities().add(vulnerability);
            }
        }
        return metaData;
    }

    @Override
    protected ImportSupport createImportSupport() {
        /* @formatter:off */
		return ImportSupport.
							builder().
								productId("PDS_INFRASCAN").
								mustBeJSON().
								contentIdentifiedBy(GenericInfrascanResult.ID_GENERIC_INFRASCAN_RESULT).
								build();
		/* @formatter:on */
    }
}
