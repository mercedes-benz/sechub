package com.daimler.sechub.domain.scan.product.sereco;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.project.FalsePositiveEntry;
import com.daimler.sechub.domain.scan.project.FalsePositiveMetaData;
import com.daimler.sechub.domain.scan.project.FalsePositiveProjectConfiguration;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

public class SerecoFalsePositiveFinderTest {

    private SerecoFalsePositiveFinder finderToTest;
    private SerecoFalsePositiveCodeScanStrategy codeSCanStrategy;

    @Before
    public void before() throws Exception {
        finderToTest = new SerecoFalsePositiveFinder();
        
        codeSCanStrategy=mock(SerecoFalsePositiveCodeScanStrategy.class);
        finderToTest.codeSCanStrategy=codeSCanStrategy;
      
    }

    @Test
    public void code_scan_triggers_codescan_strategy() {
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
        
        /* execute*/
        finderToTest.isFound(vulnerability, metaData);
        
        /* test */
        verify(codeSCanStrategy).isFalsePositive(vulnerability, metaData);
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
        
        /* execute*/
        finderToTest.isFound(vulnerability, metaData);
        
        /*test */
        verify(codeSCanStrategy,never()).isFalsePositive(vulnerability, metaData);
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
        
        /* execute*/
        finderToTest.isFound(vulnerability, metaData);
        
        /*test */
        verify(codeSCanStrategy,never()).isFalsePositive(vulnerability, metaData);
    }

    private FalsePositiveMetaData fetchFirstEntryMetaDataOfExample3() {
        String json = ScanProductSerecoTestFileSupport.getTestfileSupport().loadTestFile("false_positives/scan_false_positve_config_example3.json");
        FalsePositiveProjectConfiguration config = FalsePositiveProjectConfiguration.fromJSONString(json);
        FalsePositiveEntry entry = config.getFalsePositives().get(0);
        assertEquals("entry-1",entry.getJobData().getComment());//sanity check, means correct entry...
        FalsePositiveMetaData metaData = entry.getMetaData();
        return metaData;
    }
}
