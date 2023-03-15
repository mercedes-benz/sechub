// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FalsePositiveEntryTest {

    private FalsePositiveEntry entry1;
    private FalsePositiveEntry entry2;

    @Before
    public void before() throws Exception {
        entry1 = new FalsePositiveEntry();
        entry2 = new FalsePositiveEntry();
    }

    @Test
    public void equals_author_diff_not_same() {
        /* prepare */
        entry1.setAuthor("author1");
        entry2.setAuthor("author2");

        /* execute + test */
        assertFalse(entry1.equals(entry2));
        assertFalse(entry2.equals(entry1));
    }

    @Test
    public void equals_author_same_so_same() {
        /* prepare */
        entry1.setAuthor("author1");
        entry2.setAuthor("author1");

        /* execute + test */
        assertTrue(entry1.equals(entry2));
        assertTrue(entry2.equals(entry1));
    }

    @Test
    public void equals_metadata_equal_so_equal() {
        /* prepare */

        FalsePositiveMetaData metaData1 = new FalsePositiveMetaData();
        metaData1.setName("name1");
        entry1.setMetaData(metaData1);

        FalsePositiveMetaData metaData2 = new FalsePositiveMetaData();
        metaData2.setName("name1");
        entry2.setMetaData(metaData2);

        /* execute + test */
        assertTrue(metaData1.equals(metaData2));
        assertTrue(metaData2.equals(metaData1));
    }

    @Test
    public void equals_metadata_NOT_equals_so_NOT_equals() {
        /* prepare */

        FalsePositiveMetaData metaData1 = new FalsePositiveMetaData();
        metaData1.setName("name1");
        entry1.setMetaData(metaData1);

        FalsePositiveMetaData metaData2 = new FalsePositiveMetaData();
        metaData2.setName("name2");
        entry2.setMetaData(metaData2);

        /* execute + test */
        assertFalse(entry1.equals(entry2));
        assertFalse(entry2.equals(entry1));
    }

    @Test
    public void equals_jobdata_NOT_equals_so_NOT_equals() {
        /* prepare */

        FalsePositiveJobData jobData1 = new FalsePositiveJobData();
        jobData1.setFindingId(1);
        entry1.setJobData(jobData1);

        FalsePositiveJobData jobData2 = new FalsePositiveJobData();
        jobData1.setFindingId(2);
        entry2.setJobData(jobData2);

        /* execute + test */
        assertFalse(entry1.equals(entry2));
        assertFalse(entry2.equals(entry1));
    }

    @Test
    public void equals_jobdata_equals_so_equals() {
        /* prepare */

        FalsePositiveJobData jobData1 = new FalsePositiveJobData();
        jobData1.setFindingId(1);
        entry1.setJobData(jobData1);

        FalsePositiveJobData jobData2 = new FalsePositiveJobData();
        jobData1.setFindingId(1);
        entry2.setJobData(jobData2);

        /* execute + test */
        assertFalse(entry1.equals(entry2));
        assertFalse(entry2.equals(entry1));
    }

}
