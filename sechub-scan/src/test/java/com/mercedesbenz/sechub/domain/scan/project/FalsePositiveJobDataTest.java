// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class FalsePositiveJobDataTest {

    private FalsePositiveJobData data1;
    private FalsePositiveJobData data2;

    @Before
    public void before() throws Exception {
        data1 = new FalsePositiveJobData();
        data2 = new FalsePositiveJobData();
    }

    @Test
    public void with_nothing_set_equals() {

        assertTrue(data1.equals(data2));
        assertTrue(data2.equals(data1));
    }

    @Test
    public void different_comments_are_still_treated_as_equal() {

        data1.setComment("comment1");
        data2.setComment("comment2");

        assertTrue(data1.equals(data2));
        assertTrue(data2.equals(data1));
    }

    @Test
    public void same_finding_id_equals() {

        data1.setFindingId(1);
        data2.setFindingId(1);

        assertTrue(data1.equals(data2));
        assertTrue(data2.equals(data1));
    }

    @Test
    public void different_finding_id_NOT_equals() {

        data1.setFindingId(1);
        data2.setFindingId(2);

        assertFalse(data1.equals(data2));
        assertFalse(data2.equals(data1));
    }

    @Test
    public void same_jobUUID_equals() {

        UUID randomUUID = UUID.randomUUID();
        data1.setJobUUID(randomUUID);
        data2.setJobUUID(randomUUID);

        assertTrue(data1.equals(data2));
        assertTrue(data2.equals(data1));
    }

    @Test
    public void different_jobUUID_NOT_equals() {

        UUID randomUUID1 = UUID.randomUUID();
        UUID randomUUID2 = UUID.randomUUID();
        data1.setJobUUID(randomUUID1);
        data2.setJobUUID(randomUUID2);

        assertFalse(data1.equals(data2));
        assertFalse(data2.equals(data1));
    }

}
