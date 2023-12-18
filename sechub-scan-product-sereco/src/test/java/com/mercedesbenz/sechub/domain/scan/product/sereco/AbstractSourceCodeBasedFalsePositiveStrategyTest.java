// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveCodeMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveCodePartMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

class AbstractSourceCodeBasedFalsePositiveStrategyTest {

    private AbstractSourceCodeBasedFalsePositiveStrategy strategyToTest;
    private SerecoSourceRelevantPartResolver relevantPartResolver;

    @BeforeEach
    void beforeEach() {

        relevantPartResolver = mock(SerecoSourceRelevantPartResolver.class);
        when(relevantPartResolver.toRelevantPart(any())).thenReturn("the-relevant-part");

        strategyToTest = new TestSourceCodeBasedFalsePositiveStrategy();
        strategyToTest.relevantPartResolver = relevantPartResolver;

    }

    @ParameterizedTest()
    @EnumSource(TestPrepartionType.class)
    void hasSameLocation_one_element_locations_are_same(TestPrepartionType type) {
        /* prepare */
        String vulnerabilityCodeLocation = "same-location";
        String metaDataCodeLocation = "same-location";

        SerecoVulnerability vulnerability = new SerecoVulnerability();
        FalsePositiveCodeMetaData metaData = new FalsePositiveCodeMetaData();

        prepareFirstEntry(vulnerability, vulnerabilityCodeLocation, metaData, metaDataCodeLocation, type);

        /* execute */
        boolean sameLocation = strategyToTest.hasSameLocation(vulnerability, metaData);

        /* test */
        if (TestPrepartionType.E_METADATA_NO_START_NO_END__VULNERABILTY_NO_START_NO_END.equals(type)) {
            /*
             * even when "same" - means no location we do not accept this! Reason: we just
             * cannot determine this as "same location" - because information is missing
             */
            assertNotSameLocation(vulnerability, metaData, sameLocation, type);
        } else {
            assertSameLocation(vulnerability, metaData, sameLocation, type);
        }

    }

    @ParameterizedTest()
    @EnumSource(TestPrepartionType.class)
    void hasSameLocation_one_element_locations_NOT_same(TestPrepartionType type) {
        /* prepare */
        String vulnerabilityCodeLocation = "other-location1";
        String metaDataCodeLocation = "other-location2";

        SerecoVulnerability vulnerability = new SerecoVulnerability();
        FalsePositiveCodeMetaData metaData = new FalsePositiveCodeMetaData();

        prepareFirstEntry(vulnerability, vulnerabilityCodeLocation, metaData, metaDataCodeLocation, type);

        /* execute */
        boolean sameLocation = strategyToTest.hasSameLocation(vulnerability, metaData);

        /* test */
        assertNotSameLocation(vulnerability, metaData, sameLocation, type);

    }

    private void assertSameLocation(SerecoVulnerability vulnerability, FalsePositiveCodeMetaData metaData, boolean sameLocation, TestPrepartionType type) {
        if (!sameLocation) {
            assertEquals(vulnerability, metaData, "Should have same location:" + type);
        }
    }

    private void assertNotSameLocation(SerecoVulnerability vulnerability, FalsePositiveCodeMetaData metaData, boolean sameLocation, TestPrepartionType type) {
        if (sameLocation) {
            assertEquals(vulnerability, metaData, "Should have NOT same location:" + type);
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void prepareFirstEntry(SerecoVulnerability vulnerability, String vulnerableCodeLocation1, FalsePositiveCodeMetaData metaData,
            String metaDataCodeLocation1, TestPrepartionType type) {

        // Simulate SERECO result
        SerecoCodeCallStackElement serecoCode1 = new SerecoCodeCallStackElement();

        switch (type) {
        case C_METADATA_START_SET_NO_END__VULNERABILTY_START_SET_END_SET:
        case D_METADATA_START_SET_NO_END__VULNERABILTY_START_SET_NO_SET:
        case B_METADATA_START_SET_END_SET__VULNERABILTY_START_SET_END_SET:
        case A_METADATA_START_SET_END_SET__VULNERABILTY_START_SET_NO_END:
            serecoCode1.setLocation(vulnerableCodeLocation1);
            vulnerability.setCode(serecoCode1);

        }
        switch (type) {
        case B_METADATA_START_SET_END_SET__VULNERABILTY_START_SET_END_SET:
        case A_METADATA_START_SET_END_SET__VULNERABILTY_START_SET_NO_END:
            SerecoCodeCallStackElement serecoCode2 = new SerecoCodeCallStackElement();
            serecoCode2.setLocation(vulnerableCodeLocation1);
            serecoCode1.setCalls(serecoCode2);
            break;
        default:
            break;

        }

        // Simulate False positive meta data from database
        FalsePositiveCodePartMetaData metaDataCodePart1 = new FalsePositiveCodePartMetaData();
        switch (type) {
        case A_METADATA_START_SET_END_SET__VULNERABILTY_START_SET_NO_END:
        case D_METADATA_START_SET_NO_END__VULNERABILTY_START_SET_NO_SET:
        case B_METADATA_START_SET_END_SET__VULNERABILTY_START_SET_END_SET:
        case C_METADATA_START_SET_NO_END__VULNERABILTY_START_SET_END_SET:
            metaDataCodePart1.setLocation(metaDataCodeLocation1);
            metaData.setStart(metaDataCodePart1);

        }
        switch (type) {
        case B_METADATA_START_SET_END_SET__VULNERABILTY_START_SET_END_SET:
        case C_METADATA_START_SET_NO_END__VULNERABILTY_START_SET_END_SET:
            FalsePositiveCodePartMetaData metaDataCodePart2 = new FalsePositiveCodePartMetaData();
            metaDataCodePart2.setLocation(metaDataCodeLocation1);
            metaData.setEnd(metaDataCodePart2);

        }

    }

    private enum TestPrepartionType {

        A_METADATA_START_SET_END_SET__VULNERABILTY_START_SET_NO_END,

        B_METADATA_START_SET_END_SET__VULNERABILTY_START_SET_END_SET,

        C_METADATA_START_SET_NO_END__VULNERABILTY_START_SET_END_SET,

        D_METADATA_START_SET_NO_END__VULNERABILTY_START_SET_NO_SET,

        E_METADATA_NO_START_NO_END__VULNERABILTY_NO_START_NO_END,

    }

    private class TestSourceCodeBasedFalsePositiveStrategy extends AbstractSourceCodeBasedFalsePositiveStrategy {

        @Override
        protected ScanType getScanType() {
            return ScanType.CODE_SCAN;
        }

    }

}
