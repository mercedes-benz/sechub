package com.daimler.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.domain.scan.project.FalsePositiveMetaData;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.daimler.sechub.sereco.metadata.SerecoWeb;
import com.daimler.sechub.sereco.metadata.SerecoWebRequest;

class SerecoFalsePositiveWebScanStrategyTest {

    private SerecoFalsePositiveWebScanStrategy strategyToTest;

    @BeforeEach
    void beforeEach() {
        strategyToTest = new SerecoFalsePositiveWebScanStrategy();
    }

    @Test
    void same_cwe_id_nothing_more_set_both_is_false_positive() {
        /* prepare */
        FalsePositiveMetaData metaData = createTestFalsePositiveMetaData();
        
        SerecoVulnerability vulnerability = createTestVulnerability();
        
        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(vulnerability, metaData);

        /* test */
        assertFalse(isFalsePositive);
    }

    private FalsePositiveMetaData createTestFalsePositiveMetaData() {
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setCweId(4711);
        return metaData;
    }

    private SerecoVulnerability createTestVulnerability() {
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        SerecoWeb web = new SerecoWeb();
        vulnerability.setWeb(web);
        
        SerecoWebRequest request = web.getRequest();
        request.setMethod("method");
        request.setTarget("target");
        request.setProtocol("protocol");
        request.setVersion("version");

        web.getResponse().setStatusCode(3333);
        
        return vulnerability;
    }

}
