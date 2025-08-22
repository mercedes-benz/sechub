// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.Severity;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanFinding;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanProductData;
import com.mercedesbenz.sechub.commons.model.interchange.GenericInfrascanResult;
import com.mercedesbenz.sechub.sereco.ImportParameter;
import com.mercedesbenz.sechub.sereco.metadata.SerecoMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

class GenericInfrascanResultImporterTest {

    private GenericInfrascanResultImporter importerToTest;

    @BeforeEach
    void before() {
        importerToTest = new GenericInfrascanResultImporter();
    }

    @Test
    void empty_generic_infrascan_result_json_with_product_pds_infrascan_id_is_able_to_import() {
        /* prepare */
        GenericInfrascanResult result = new GenericInfrascanResult();
        String json = JSONConverter.get().toJSON(result);

        ImportParameter param = ImportParameter.builder().importData(json).importId("id1").productId("PDS_INFRASCAN").build();

        /* execute */
        boolean ableToImport = importerToTest.isAbleToImportForProduct(param);

        /* test */
        assertTrue(ableToImport, "Was not able to import json!");
    }
    
    @Test
    void jsonReportFromPDS_Infrascan_canBeImported() throws Exception {
        /* prepare */
        GenericInfrascanResult result = new GenericInfrascanResult();
        GenericInfrascanProductData product1 = new GenericInfrascanProductData();
        GenericInfrascanFinding finding1 = new GenericInfrascanFinding();
        finding1.setCveId("CVE-12345");
        finding1.setName("finding-test1");
        finding1.setSeverity(Severity.INFO);
        product1.getFindings().add(finding1);
        result.getProducts().add(product1);
        
        String json = JSONConverter.get().toJSON(result);
        
        /* execute */
        SerecoMetaData importResult = importerToTest.importResult(json, ScanType.IAC_SCAN);
        
        /* test */
        assertThat(importResult).isNotNull();
        assertThat(importResult.getVulnerabilities()).hasSize(1);
        SerecoVulnerability finding = importResult.getVulnerabilities().iterator().next();
        assertThat(finding).isNotNull();
        assertThat(finding.getSeverity()).isEqualTo(SerecoSeverity.INFO);
        assertThat(finding.getType()).isEqualTo("finding-test1");
        
    }
    @Test
    void jsonReportFromPDS_Infrascan_2_products_canBeImported() throws Exception {
        /* prepare */
        GenericInfrascanResult result = new GenericInfrascanResult();
        GenericInfrascanProductData product1 = new GenericInfrascanProductData();
        GenericInfrascanFinding finding1 = new GenericInfrascanFinding();
        finding1.setCveId("CVE-12345");
        finding1.setName("finding-test1");
        finding1.setSeverity(Severity.INFO);
        product1.getFindings().add(finding1);
        result.getProducts().add(product1);
        
        GenericInfrascanProductData product2 = new GenericInfrascanProductData();
        GenericInfrascanFinding finding2 = new GenericInfrascanFinding();
        finding2.setCveId("CVE-678");
        finding2.setName("finding-test2");
        finding2.setSeverity(Severity.MEDIUM);
        product2.getFindings().add(finding2);
        result.getProducts().add(product2);
        
        String json = JSONConverter.get().toJSON(result);
        
        /* execute */
        SerecoMetaData importResult = importerToTest.importResult(json, ScanType.IAC_SCAN);
        
        /* test */
        assertThat(importResult).isNotNull();
        assertThat(importResult.getVulnerabilities()).hasSize(2);
        Iterator<SerecoVulnerability> iterator = importResult.getVulnerabilities().iterator();
        SerecoVulnerability finding = iterator.next();
        assertThat(finding).isNotNull();
        assertThat(finding.getSeverity()).isEqualTo(SerecoSeverity.INFO);
        assertThat(finding.getType()).isEqualTo("finding-test1");
        
        finding = iterator.next();
        assertThat(finding).isNotNull();
        assertThat(finding.getSeverity()).isEqualTo(SerecoSeverity.MEDIUM);
        assertThat(finding.getType()).isEqualTo("finding-test2");
        
    }

}
