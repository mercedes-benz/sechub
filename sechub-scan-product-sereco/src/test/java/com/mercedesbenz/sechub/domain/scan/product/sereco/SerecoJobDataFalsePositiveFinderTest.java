// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveEntry;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectConfiguration;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

public class SerecoJobDataFalsePositiveFinderTest {

    private SerecoJobDataFalsePositiveFinder finderToTest;
    private CodeScanJobDataFalsePositiveStrategy jobDataCodeScanStrategy;
    private WebScanJobDataFalsePositiveStrategy jobDataWebScanStrategy;
    private IacScanJobDataFalsePositiveStrategy jobDataIacScanStrategy;

    // we use always true here, because every mock will return false when
    // not defined. Only some "syntactic sugar" to make test easier to read
    private final boolean yesItIsAFalsePositive = true;

    @BeforeEach
    void beforeEach() throws Exception {
        finderToTest = new SerecoJobDataFalsePositiveFinder();

        jobDataCodeScanStrategy = mock(CodeScanJobDataFalsePositiveStrategy.class);
        jobDataIacScanStrategy = mock(IacScanJobDataFalsePositiveStrategy.class);
        jobDataWebScanStrategy = mock(WebScanJobDataFalsePositiveStrategy.class);

        finderToTest.jobDataCodeScanStrategy = jobDataCodeScanStrategy;
        finderToTest.jobDataIacScanStrategy = jobDataIacScanStrategy;
        finderToTest.jobDataWebScanStrategy = jobDataWebScanStrategy;
    }

    @Test
    void code_scan_triggers_codescan_strategy_and_uses_its_result() {
        /* prepare */
        /* @formatter:off */
        SerecoVulnerability vulnerability = TestSerecoVulnerabilityBuilder.builder().
            name("name1").
            codeScan().
                location("location1").
                source("source1").
                relevantPart("relevant1").
                callsCode().
                    callsCode().
                        callsCode().
                            location("location2").
                            source("source2").
                            relevantPart("relevant2").
               end().build();
        /* @formatter:on */

        FalsePositiveMetaData metaData = fetchFirstEntryMetaDataOfExample3();

        when(jobDataCodeScanStrategy.isFalsePositive(vulnerability, metaData)).thenReturn(yesItIsAFalsePositive);

        /* execute */
        boolean strategyResult = finderToTest.isFound(vulnerability, metaData);

        /* test */
        verify(jobDataCodeScanStrategy).isFalsePositive(vulnerability, metaData);
        assertEquals(yesItIsAFalsePositive, strategyResult);
    }

    @Test
    void iac_scan_triggers_iacscan_strategy_and_uses_its_result() {
        /* prepare */
        /* @formatter:off */
        SerecoVulnerability vulnerability = TestSerecoVulnerabilityBuilder.builder().
                name("name1").
                iacScan().
                location("location1").
                source("source1").
                relevantPart("relevant1").
                end().build();
        /* @formatter:on */

        FalsePositiveMetaData metaData = fetchFirstEntryMetaDataOfExample3();

        when(jobDataIacScanStrategy.isFalsePositive(vulnerability, metaData)).thenReturn(yesItIsAFalsePositive);

        /* execute */
        boolean strategyResult = finderToTest.isFound(vulnerability, metaData);

        /* test */
        verify(jobDataIacScanStrategy).isFalsePositive(vulnerability, metaData);
        assertEquals(yesItIsAFalsePositive, strategyResult);
    }

    @Test
    void web_scan_triggers_webscan_strategy_and_uses_its_result() {
        /* prepare */
        /* @formatter:off */
        SerecoVulnerability vulnerability = TestSerecoVulnerabilityBuilder.builder().
            name("name1").
            webScan().end().
            build();
        /* @formatter:on */

        FalsePositiveMetaData metaData = fetchFirstEntryMetaDataOfExample3();

        when(jobDataWebScanStrategy.isFalsePositive(vulnerability, metaData)).thenReturn(yesItIsAFalsePositive);

        /* execute */
        boolean strategyResult = finderToTest.isFound(vulnerability, metaData);

        /* test */
        verify(jobDataWebScanStrategy).isFalsePositive(vulnerability, metaData);
        assertEquals(yesItIsAFalsePositive, strategyResult);

        // additional check that other strategy is not called here
        verify(jobDataCodeScanStrategy, never()).isFalsePositive(vulnerability, metaData);
    }

    @Test
    void webscan_triggers_not_codescanstrategy() {
        /* prepare */
        /* @formatter:off */
        SerecoVulnerability vulnerability = TestSerecoVulnerabilityBuilder.builder().
            name("name1").
            webScan().
               end().build();

        /* @formatter:on */

        FalsePositiveMetaData metaData = fetchFirstEntryMetaDataOfExample3();

        /* execute */
        finderToTest.isFound(vulnerability, metaData);

        /* test */
        verify(jobDataCodeScanStrategy, never()).isFalsePositive(vulnerability, metaData);
    }

    @Test
    void infrascan_triggers_not_codescanstrategy() {
        /* prepare */
        /* @formatter:off */
        SerecoVulnerability vulnerability = TestSerecoVulnerabilityBuilder.builder().
            name("name1").
            infraScan().
               end().build();

        /* @formatter:on */

        FalsePositiveMetaData metaData = fetchFirstEntryMetaDataOfExample3();

        /* execute */
        finderToTest.isFound(vulnerability, metaData);

        /* test */
        verify(jobDataCodeScanStrategy, never()).isFalsePositive(vulnerability, metaData);
    }

    private FalsePositiveMetaData fetchFirstEntryMetaDataOfExample3() {
        String json = TestScanProductSerecoFileSupport.getTestfileSupport().loadTestFile("false_positives/scan_false_positive_config_example3.json");
        FalsePositiveProjectConfiguration config = FalsePositiveProjectConfiguration.fromJSONString(json);
        FalsePositiveEntry entry = config.getFalsePositives().get(0);
        assertEquals("entry-1", entry.getJobData().getComment());// sanity check, means correct entry...
        FalsePositiveMetaData metaData = entry.getMetaData();
        return metaData;
    }
}
