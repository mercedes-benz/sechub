package com.daimler.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.domain.scan.project.FalsePositiveMetaData;
import com.daimler.sechub.domain.scan.project.FalsePositiveWebMetaData;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.daimler.sechub.sereco.metadata.SerecoWeb;
import com.daimler.sechub.sereco.metadata.SerecoWebRequest;

class SerecoFalsePositiveWebScanStrategyTest {

    private static final int CWE_ID_4711 = 4711;
    private SerecoFalsePositiveWebScanStrategy strategyToTest;

    @BeforeEach
    void beforeEach() {
        strategyToTest = new SerecoFalsePositiveWebScanStrategy();
    }

    @DisplayName("Not false positive. Nearly valid false positive, but other cweId in meta data")
    @Test
    void no_false_positive_because_missing_data1() {
        /* prepare */
        FalsePositiveMetaData metaData = createValidTestFalsePositiveMetaData();
        metaData.setCweId(CWE_ID_4711 + 1);

        SerecoVulnerability vulnerability = createValidTestVulnerability();

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(vulnerability, metaData);

        /* test */
        assertFalse(isFalsePositive);
    }

    @DisplayName("Not false positive. Nearly valid false positive, but cweId in vulnerability is defined as 'other'")
    @Test
    void no_false_positive_because_missing_data2() {
        /* prepare */
        FalsePositiveMetaData metaData = createValidTestFalsePositiveMetaData();

        SerecoVulnerability vulnerability = createValidTestVulnerability();
        vulnerability.getClassification().setCwe("other");

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(vulnerability, metaData);

        /* test */
        assertFalse(isFalsePositive);
    }

    @DisplayName("Not false positive. Same CWE id in vulnerability and metadata defined. Scan type set, but no web metata available. Must result in NO fp")
    @Test
    void no_false_positive_because_missing_data3() {
        /* prepare */
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setCweId(4711);
        metaData.setWeb(new FalsePositiveWebMetaData());

        SerecoVulnerability vulnerability = new SerecoVulnerability();
        vulnerability.getClassification().setCwe("4711");
        vulnerability.setScanType(ScanType.WEB_SCAN);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(vulnerability, metaData);

        /* test */
        assertFalse(isFalsePositive);
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class, names = "WEB_SCAN", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Not false positive. Nearly valid false positive, but not marked because wrong scan type in meta data defined")
    @NullSource
    void no_false_positive_because_wrong_scan_type(ScanType type) {
        /* prepare */
        FalsePositiveMetaData metaData = createValidTestFalsePositiveMetaData();
        SerecoVulnerability vulnerability = createValidTestVulnerability();

        vulnerability.setScanType(type);// but we change scan type...

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(vulnerability, metaData);

        /* test */
        assertFalse(isFalsePositive);// no longer found
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class, names = "WEB_SCAN", mode = EnumSource.Mode.INCLUDE)
    @DisplayName("IS false positive. Valid false positive defined, scan type is correct")
    void is_false_positive_because_correct_scan_type(ScanType type) {
        /* prepare */
        FalsePositiveMetaData metaData = createValidTestFalsePositiveMetaData();
        SerecoVulnerability vulnerability = createValidTestVulnerability();

        vulnerability.setScanType(type);// we change scan type...(if not already set)

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(vulnerability, metaData);

        /* test */
        assertTrue(isFalsePositive);// no longer found
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ..................Helpers....................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    private FalsePositiveMetaData createValidTestFalsePositiveMetaData() {
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setCweId(4711);
        metaData.setScanType(ScanType.WEB_SCAN);
        FalsePositiveWebMetaData web = new FalsePositiveWebMetaData();
        metaData.setWeb(web);
        return metaData;
    }

    private SerecoVulnerability createValidTestVulnerability() {
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        SerecoWeb web = new SerecoWeb();
        vulnerability.getClassification().setCwe(""+CWE_ID_4711);
        vulnerability.setWeb(web);
        vulnerability.setScanType(ScanType.WEB_SCAN);
        

        SerecoWebRequest request = web.getRequest();
        request.setMethod("method");
        request.setTarget("target");
        request.setProtocol("protocol");
        request.setVersion("version");

        web.getResponse().setStatusCode(3333);

        return vulnerability;
    }

}
