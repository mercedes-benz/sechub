// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveWebMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveWebRequestMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveWebResponseMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebEvidence;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWebRequest;

class SerecoFalsePositiveWebScanStrategyTest {

    private static final String METHOD1 = "method1";
    private static final String EVIDENCE1 = "evidence1";
    private static final String TARGET1 = "target1";
    private static final String ATTACK_VECTOR1 = "vector1";
    private static final int CWE_ID_4711 = 4711;
    private SerecoFalsePositiveWebScanStrategy strategyToTest;
    private FalsePositivedTestDataContainer testData;

    @BeforeEach
    void beforeEach() {
        strategyToTest = new SerecoFalsePositiveWebScanStrategy();

        // Initial this created test data contains meta and vulnerability data which do
        // match. When not changed, this must lead to a false positive detection. Tests
        // do change this data to simulate different situations.
        testData = createInitialTestDataWithMatchingVulnerabilityAndFalsePositiveDefinition();
    }

    /* @formatter:off */

    /* --------------------------------------------------------------------------------*/
    /* -----------NetworkTarget tests---------------------------------------------------------*/
    /* --------------------------------------------------------------------------------*/
    @DisplayName("Not false positive. NetworkTarget changed to other value in metadata")
    @NullSource
    @EmptySource
    @CsvSource({"other-target", ATTACK_VECTOR1+"."})
    @ParameterizedTest
    void no_false_positive_because_metadata_has_not_target_like_vulnerability(String target) {
        /* prepare */
        testData.metaData.getWeb().getRequest().setTarget(target);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertFalse(isFalsePositive);
    }

    @DisplayName("Is false positive. NetworkTarget changed to similar value in metadata")
    @CsvSource({TARGET1+"\t", TARGET1+" "})
    @ParameterizedTest
    void is_false_positive_because_metadata_has_same_target_like_vulnerability(String target) {
        /* prepare */
        testData.metaData.getWeb().getRequest().setTarget(target);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertTrue(isFalsePositive);
    }

    /* --------------------------------------------------------------------------------*/
    /* -----------Method tests---------------------------------------------------------*/
    /* --------------------------------------------------------------------------------*/
    @DisplayName("Not false positive. Method changed to other value in metadata")
    @NullSource
    @EmptySource
    @CsvSource({"other-method", METHOD1+"X"})
    @ParameterizedTest
    void no_false_positive_because_metadata_has_not_method_like_vulnerability(String method) {
        /* prepare */
        testData.metaData.getWeb().getRequest().setMethod(method);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertFalse(isFalsePositive);
    }

    @DisplayName("Is false positive. Method changed to similar value in metadata")
    @CsvSource({METHOD1+"  ", " "+METHOD1+" "})
    @ParameterizedTest
    void is_false_positive_because_metadata_has_similar_method_like_vulnerability(String method) {
        /* prepare */
        testData.metaData.getWeb().getRequest().setMethod(method);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertTrue(isFalsePositive);
    }

    /* --------------------------------------------------------------------------------*/
    /* -----------Attack vector tests---------------------------------------------------*/
    /* --------------------------------------------------------------------------------*/
    @DisplayName("Not false positive. Attack vector changed to other value in metadata")
    @NullSource
    @EmptySource
    @CsvSource({"other-atack", ATTACK_VECTOR1+"."})
    @ParameterizedTest
    void no_false_positive_because_metadata_has_not_attack_like_vulnerability(String vector) {
        /* prepare */
        testData.metaData.getWeb().getRequest().setAttackVector(vector);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertFalse(isFalsePositive);
    }

    @DisplayName("Is false positive. Attack vector changed to similar value in metadata")
    @CsvSource({ATTACK_VECTOR1+"\t", ATTACK_VECTOR1+" ","  \t"+ATTACK_VECTOR1 })
    @ParameterizedTest
    void is_false_positive_because_metadata_has_similar_attack_like_vulnerability(String vector) {
        /* prepare */
        testData.metaData.getWeb().getRequest().setAttackVector(vector);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertTrue(isFalsePositive);
    }


    /* --------------------------------------------------------------------------------*/
    /* -----------Evidence tests-------------------------------------------------------*/
    /* --------------------------------------------------------------------------------*/
    @DisplayName("Not false positive. Evidence changed to other value in metadata")
    @NullSource
    @EmptySource
    @CsvSource({"other-atack", EVIDENCE1+"."})
    @ParameterizedTest
    void no_false_positive_because_metadata_has_not_evidence_like_vulnerability(String evidence) {
        /* prepare */
        testData.metaData.getWeb().getResponse().setEvidence(evidence);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertFalse(isFalsePositive);
    }

    @DisplayName("Is false positive. Evidence changed to similar value in metadata")
    @CsvSource({EVIDENCE1+"\t", EVIDENCE1+" ","  \t"+EVIDENCE1 })
    @ParameterizedTest
    void is_false_positive_because_metadata_has_similar_evidence_like_vulnerability(String evidence) {
        /* prepare */
        testData.metaData.getWeb().getResponse().setEvidence(evidence);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertTrue(isFalsePositive);
    }


    /* --------------------------------------------------------------------------------*/
    /* -----------CWE tests------------------------------------------------------------*/
    /* --------------------------------------------------------------------------------*/
    @DisplayName("Not false positive. Nearly valid false positive, but other cweId in meta data")
    @Test
    void no_false_positive_because_wrong_metadata_cweid() {
        /* prepare */
        testData.metaData.setCweId(CWE_ID_4711 + 1);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertFalse(isFalsePositive);
    }

    @DisplayName("Not false positive. Nearly valid false positive, but cweId in vulnerability is defined as 'other'")
    @Test
    void no_false_positive_because_wrong_cwe_other() {
        /* prepare */
        testData.vulnerability.getClassification().setCwe("other");

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertFalse(isFalsePositive);
    }


    /* --------------------------------------------------------------------------------*/
    /* -----------ScanType tests-------------------------------------------------------*/
    /* --------------------------------------------------------------------------------*/
    @ParameterizedTest
    @EnumSource(value = ScanType.class, names = "WEB_SCAN", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Not false positive. Nearly valid false positive, but not marked because wrong scan type in meta data defined")
    @NullSource
    void no_false_positive_because_wrong_scan_type(ScanType type) {
        /* prepare */
        testData.vulnerability.setScanType(type);// but we change scan type...

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertFalse(isFalsePositive);// no longer found
    }

    @ParameterizedTest
    @EnumSource(value = ScanType.class, names = "WEB_SCAN", mode = EnumSource.Mode.INCLUDE)
    @DisplayName("IS false positive. Valid false positive defined, scan type is correct")
    void is_false_positive_because_correct_scan_type(ScanType type) {
        /* prepare */
        testData.vulnerability.setScanType(type);// we change scan type...(if not already set)

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(testData.vulnerability, testData.metaData);

        /* test */
        assertTrue(isFalsePositive);// no longer found
    }

    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* + ..................Helpers....................... + */
    /* ++++++++++++++++++++++++++++++++++++++++++++++++++++ */
    /* @formatter:on */

    private FalsePositivedTestDataContainer createInitialTestDataWithMatchingVulnerabilityAndFalsePositiveDefinition() {
        return new FalsePositivedTestDataContainer();
    }

    private class FalsePositivedTestDataContainer {
        FalsePositiveMetaData metaData = createValidTestFalsePositiveMetaData();
        SerecoVulnerability vulnerability = createValidTestVulnerability();
    }

    private FalsePositiveMetaData createValidTestFalsePositiveMetaData() {
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setCweId(4711);
        metaData.setScanType(ScanType.WEB_SCAN);
        FalsePositiveWebMetaData web = new FalsePositiveWebMetaData();
        metaData.setWeb(web);

        FalsePositiveWebRequestMetaData metaDataWebRequest = web.getRequest();
        metaDataWebRequest.setAttackVector(ATTACK_VECTOR1);
        metaDataWebRequest.setMethod(METHOD1);
        metaDataWebRequest.setProtocol("protocol1");
        metaDataWebRequest.setTarget(TARGET1);
        metaDataWebRequest.setVersion("version1");
        FalsePositiveWebResponseMetaData metaDataWebResponse = web.getResponse();
        metaDataWebResponse.setEvidence(EVIDENCE1);

        return metaData;
    }

    private SerecoVulnerability createValidTestVulnerability() {
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        SerecoWeb web = new SerecoWeb();
        vulnerability.getClassification().setCwe("" + CWE_ID_4711);
        vulnerability.setWeb(web);
        vulnerability.setScanType(ScanType.WEB_SCAN);

        SerecoWebRequest request = web.getRequest();
        request.setMethod(METHOD1);
        request.setTarget(TARGET1);
        request.setProtocol("protocol1");
        request.setVersion("version1");

        web.getResponse().setStatusCode(3333);
        web.getAttack().setVector(ATTACK_VECTOR1);

        SerecoWebEvidence evidence = new SerecoWebEvidence();
        web.getAttack().setEvidence(evidence);
        evidence.setSnippet(EVIDENCE1);
        return vulnerability;
    }

}
