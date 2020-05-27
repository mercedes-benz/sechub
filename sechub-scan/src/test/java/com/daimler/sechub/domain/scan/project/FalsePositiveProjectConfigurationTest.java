package com.daimler.sechub.domain.scan.project;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.domain.scan.Severity;
import com.daimler.sechub.sharedkernel.type.ScanType;

public class FalsePositiveProjectConfigurationTest {

    private FalsePositiveProjectConfiguration configToTest;

    @Before
    public void before() throws Exception {
        configToTest = new FalsePositiveProjectConfiguration(); 
    }

    @Test
    public void marshal_and_unmarshal_contains_same_content() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();

        FalsePositiveEntry entry = new FalsePositiveEntry();
        FalsePositiveJobData jobData = new FalsePositiveJobData();
        jobData.setComment("comment1");
        jobData.setFindingId(42);
        jobData.setJobUUID(jobUUID);
        List<FalsePositiveEntry> falsePositives = configToTest.getFalsePositives();
        falsePositives.add(entry);
        
        FalsePositiveCodeMetaData code = new FalsePositiveCodeMetaData();
        
        FalsePositiveCodePartMetaData start = new FalsePositiveCodePartMetaData();
        start.setLocation("location1");
        start.setRelevantPart("relevant1");
        start.setSourceCode("source2");
        
        FalsePositiveCodePartMetaData end = new FalsePositiveCodePartMetaData();
        end.setLocation("location1");
        end.setRelevantPart("relevant1");
        end.setSourceCode("source2");
        code.setEnd(end);
        
        FalsePositiveMetaData metaData = new FalsePositiveMetaData();
        metaData.setName("name1");
        metaData.setScanType(ScanType.CODE_SCAN);
        metaData.setSeverity(Severity.MEDIUM);
        metaData.setCode(code);

        entry.setAuthor("testuser1");
        entry.setJobData(jobData);
        entry.setMetaData(metaData);
        
        
        /* execute */
        String json = configToTest.toJSON();
        
        
        /* test */
        FalsePositiveProjectConfiguration loaded = FalsePositiveProjectConfiguration.fromJSONString(json);
        List<FalsePositiveEntry> loadedFalsePositives = loaded.getFalsePositives();
        assertEquals(1, loadedFalsePositives.size());
        FalsePositiveEntry loadedEntry = loadedFalsePositives.iterator().next();
        assertEquals(entry,loadedEntry);
        assertEquals("testuser1", loadedEntry.getAuthor());
        
    }

}
