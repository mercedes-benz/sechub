// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveEntry;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectConfiguration;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

public class SerecoFalsePositiveFinderTest {

    private SerecoFalsePositiveFinder finderToTest;
    private SerecoFalsePositiveCodeScanStrategy codeScanStrategy;
    private SerecoFalsePositiveWebScanStrategy webScanStrategy;

    // we use always true here, because every mock will return false when
    // not defined. Only some "syntactic sugar" to make test easier to read
    private final boolean yesItIsAFalsePositive = true;

    @Before
    public void before() throws Exception {
        finderToTest = new SerecoFalsePositiveFinder();

        codeScanStrategy = mock(SerecoFalsePositiveCodeScanStrategy.class);
        finderToTest.codeScanStrategy = codeScanStrategy;

        webScanStrategy = mock(SerecoFalsePositiveWebScanStrategy.class);
        finderToTest.webScanStrategy = webScanStrategy;

    }

    @Test
    public void code_scan_triggers_codescan_strategy_and_uses_its_result() {
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

        when(codeScanStrategy.isFalsePositive(vulnerability, metaData)).thenReturn(yesItIsAFalsePositive);

        /* execute */
        boolean strategyResult = finderToTest.isFound(vulnerability, metaData);

        /* test */
        verify(codeScanStrategy).isFalsePositive(vulnerability, metaData);
        assertEquals(yesItIsAFalsePositive, strategyResult);
    }

    @Test
    public void web_scan_triggers_webscan_strategy_and_uses_its_result() {
        /* prepare */
        /* @formatter:off */
        SerecoVulnerability vulnerability = TestSerecoVulnerabilityBuilder.builder().
            name("name1").
            webScan().end().
            build();
        /* @formatter:on */

        FalsePositiveMetaData metaData = fetchFirstEntryMetaDataOfExample3();

        when(webScanStrategy.isFalsePositive(vulnerability, metaData)).thenReturn(yesItIsAFalsePositive);

        /* execute */
        boolean strategyResult = finderToTest.isFound(vulnerability, metaData);

        /* test */
        verify(webScanStrategy).isFalsePositive(vulnerability, metaData);
        assertEquals(yesItIsAFalsePositive, strategyResult);

        // additional check that other strategy is not called here
        verify(codeScanStrategy, never()).isFalsePositive(vulnerability, metaData);
    }

    @Test
    public void webscan_triggers_not_codescanstrategy() {
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
        verify(codeScanStrategy, never()).isFalsePositive(vulnerability, metaData);
    }

    @Test
    public void infrascan_triggers_not_codescanstrategy() {
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
        verify(codeScanStrategy, never()).isFalsePositive(vulnerability, metaData);
    }

    private FalsePositiveMetaData fetchFirstEntryMetaDataOfExample3() {
        String json = ScanProductSerecoTestFileSupport.getTestfileSupport().loadTestFile("false_positives/scan_false_positive_config_example3.json");
        FalsePositiveProjectConfiguration config = FalsePositiveProjectConfiguration.fromJSONString(json);
        FalsePositiveEntry entry = config.getFalsePositives().get(0);
        assertEquals("entry-1", entry.getJobData().getComment());// sanity check, means correct entry...
        FalsePositiveMetaData metaData = entry.getMetaData();
        return metaData;
    }
}
