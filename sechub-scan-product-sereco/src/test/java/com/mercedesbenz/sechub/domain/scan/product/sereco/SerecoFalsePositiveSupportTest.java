// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

class SerecoFalsePositiveSupportTest {
    private SerecoJobDataFalsePositiveSupport supportToTest;

    @BeforeEach
    void beforeEach() {
        supportToTest = new SerecoJobDataFalsePositiveSupport();
    }

    @ParameterizedTest()
    @EnumSource(ScanType.class)
    @NullSource
    void hasSameScanType(ScanType scanType) {
        /* prepare */
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        vulnerability.setScanType(scanType);

        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setScanType(scanType);

        /* execute */
        boolean result = supportToTest.areBothHavingExpectedScanType(scanType, metaData, vulnerability);

        /* test */
        assertTrue(result);
    }

    @ParameterizedTest()
    @EnumSource(ScanType.class)
    @NullSource
    void hasNotSameScanType_vulnerablity_has_different_than(ScanType scanType) {
        /* prepare */
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();

        for (ScanType other : ScanType.values()) {
            if (other == scanType) {
                continue;
            }
            vulnerability.setScanType(other);
            metaData.setScanType(scanType);

            /* execute */
            boolean result = supportToTest.areBothHavingExpectedScanType(scanType, metaData, vulnerability);

            /* test */
            assertFalse(result);
        }
    }

    @ParameterizedTest()
    @EnumSource(ScanType.class)
    @NullSource
    void hasNotSameScanType_metadata_has_different_than(ScanType scanType) {
        /* prepare */
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();

        for (ScanType other : ScanType.values()) {
            if (other == scanType) {
                continue;
            }
            vulnerability.setScanType(scanType);
            metaData.setScanType(other);

            /* execute */
            boolean result = supportToTest.areBothHavingExpectedScanType(scanType, metaData, vulnerability);

            /* test */
            assertFalse(result);
        }
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = { "1", "-1", "0", "4711" })
    void hasEitherSameCweIdOrBothNone_returns_true_when_both_are_same(String cweId) {
        /* prepare */
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        vulnerability.getClassification().setCwe(cweId);

        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setCweId(createAsInt(cweId));

        /* execute */
        boolean result = supportToTest.areBothHavingSameCweIdOrBothNoCweId(metaData, vulnerability);

        /* test */
        assertTrue(result);

    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = { "1", "-1", "0", "4711" })
    void hasEitherSameCweIdOrBothNone_returns_false_when_meta_is_one_more(String cweId) {
        /* prepare */
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        vulnerability.getClassification().setCwe(cweId);

        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setCweId(createAsIntButOneMore(cweId));

        /* execute */
        boolean result = supportToTest.areBothHavingSameCweIdOrBothNoCweId(metaData, vulnerability);

        /* test */
        assertFalse(result);

    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 1, -1, 0, 4711 })
    void hasEitherSameCweIdOrBothNone_returns_false_when_vulnerability_is_one_more(Integer cweId) {
        /* prepare */
        SerecoVulnerability vulnerability = new SerecoVulnerability();
        vulnerability.getClassification().setCwe(createAsIntStringButOneMore(cweId));

        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setCweId(cweId);

        /* execute */
        boolean result = supportToTest.areBothHavingSameCweIdOrBothNoCweId(metaData, vulnerability);

        /* test */
        assertFalse(result);

    }

    private String createAsIntStringButOneMore(Integer cweId) {
        if (cweId == null) {
            return "1";
        }
        int next = cweId.intValue() + 1;
        return String.valueOf(next);
    }

    private Integer createAsIntButOneMore(String cweId) {
        Integer intvalue = createAsInt(cweId);
        if (intvalue == null) {
            return 1;
        }
        return intvalue + 1;
    }

    private Integer createAsInt(String cweId) {
        if (cweId == null) {
            return null;
        }
        if (cweId.isEmpty()) {
            return null;
        }
        return Integer.parseInt(cweId);
    }

}
