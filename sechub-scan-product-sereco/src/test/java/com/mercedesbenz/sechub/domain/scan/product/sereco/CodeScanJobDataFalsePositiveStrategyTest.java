// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveEntry;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectConfiguration;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

public class CodeScanJobDataFalsePositiveStrategyTest {

    private CodeScanJobDataFalsePositiveStrategy strategyToTest;
    private SerecoSourceRelevantPartResolver relevantPartResolver;
    private SerecoJobDataFalsePositiveSupport serecoJobDataFalsePositiveSupport;

    @Before
    public void before() throws Exception {
        strategyToTest = new CodeScanJobDataFalsePositiveStrategy();

        relevantPartResolver = mock(SerecoSourceRelevantPartResolver.class);
        serecoJobDataFalsePositiveSupport = mock(SerecoJobDataFalsePositiveSupport.class);

        strategyToTest.relevantPartResolver = relevantPartResolver;
        strategyToTest.falsePositiveSupport = serecoJobDataFalsePositiveSupport;

    }

    @Test
    public void vulnerability_is_found_when_locations_and_relevant_parts_are_same() {
        /* prepare */
        /* @formatter:off */
        SerecoVulnerability vulnerability = TestSerecoVulnerabilityBuilder.builder().
            name("name1").
            cwe(1).
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
        when(relevantPartResolver.toRelevantPart(any())).thenReturn("");

        when(serecoJobDataFalsePositiveSupport.areBothHavingExpectedScanType(ScanType.CODE_SCAN, metaData, vulnerability)).thenReturn(true);
        when(serecoJobDataFalsePositiveSupport.areBothHavingSameCweIdOrBothNoCweId(metaData, vulnerability)).thenReturn(true);

        /* execute + test */
        assertTrue(strategyToTest.isFalsePositive(vulnerability, metaData));
    }

    @Test
    public void vulnerability_is_NOT_found_when_locations_and_relevant_parts_are_same_but_cwe_differs() {
        /* prepare */
        /* @formatter:off */
        SerecoVulnerability vulnerability = TestSerecoVulnerabilityBuilder.builder().
            name("name1").
            cwe(4711).
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
        when(relevantPartResolver.toRelevantPart(any())).thenReturn("");
        when(serecoJobDataFalsePositiveSupport.areBothHavingExpectedScanType(ScanType.CODE_SCAN, metaData, vulnerability)).thenReturn(true);
        when(serecoJobDataFalsePositiveSupport.areBothHavingSameCweIdOrBothNoCweId(metaData, vulnerability)).thenReturn(false);

        /* execute + test */
        assertFalse(strategyToTest.isFalsePositive(vulnerability, metaData));
    }

    @Test
    public void vulnerability_is_NOT_found_when_start_location_differs_and_relevant_parts_are_same() {
        /* prepare */
        /* @formatter:off */
        SerecoVulnerability vulnerability = TestSerecoVulnerabilityBuilder.builder().
            name("name1").
            cwe(1).
            codeScan().
                location("location-other-1").// here different to false-positive meta data! So may not be found!
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
        when(relevantPartResolver.toRelevantPart(any())).thenReturn("");

        when(serecoJobDataFalsePositiveSupport.areBothHavingExpectedScanType(ScanType.CODE_SCAN, metaData, vulnerability)).thenReturn(true);
        when(serecoJobDataFalsePositiveSupport.areBothHavingSameCweIdOrBothNoCweId(metaData, vulnerability)).thenReturn(true);

        /* execute + test */
        assertFalse(strategyToTest.isFalsePositive(vulnerability, metaData));
    }

    @Test
    public void vulnerability_having_no_relevant_part_will_use_relevant_part_resolver_on_start_and_end() {
        /* prepare */
        /* @formatter:off */
        SerecoVulnerability vulnerability = TestSerecoVulnerabilityBuilder.builder().
            name("name1").
            cwe(1).
            codeScan().
                location("location1").// here different to false-positive meta data! So may not be found!
                source("source1").
                callsCode().
                    callsCode().
                        callsCode().
                            location("location2").
                            source("source2").
               end().build();

        /* @formatter:on */

        FalsePositiveMetaData metaData = fetchFirstEntryMetaDataOfExample3();
        when(relevantPartResolver.toRelevantPart("source1")).thenReturn("relevant1");
        when(relevantPartResolver.toRelevantPart("source2")).thenReturn("relevant2");

        when(serecoJobDataFalsePositiveSupport.areBothHavingExpectedScanType(ScanType.CODE_SCAN, metaData, vulnerability)).thenReturn(true);
        when(serecoJobDataFalsePositiveSupport.areBothHavingSameCweIdOrBothNoCweId(metaData, vulnerability)).thenReturn(true);

        /* execute */
        boolean isFalsePositive = strategyToTest.isFalsePositive(vulnerability, metaData);

        /* test */
        verify(relevantPartResolver).toRelevantPart("source1");
        verify(relevantPartResolver).toRelevantPart("source2");
        assertTrue(isFalsePositive);
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
