// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { ScanReportRepository.class, ScanReportRepositoryDBTest.SimpleTestConfiguration.class })
public class ScanReportRepositoryDBTest {

    @Autowired
    private ScanReportRepository repositoryToTest;

    @Before
    public void before() {
    }

    @TestConfiguration
    @EnableAutoConfiguration
    public static class SimpleTestConfiguration {

    }

    @Test
    public void given_3_stored_scan_reports_2_for_project1_1_for_project2_a_delete_all_for_project1_does_only_delete_project1_parts() throws Exception {
        /* prepare */
        UUID job1_project1 = UUID.randomUUID();
        UUID job2_project2 = UUID.randomUUID();
        UUID job3_project1 = UUID.randomUUID();

        ScanReport result1 = new ScanReport(job1_project1, "project1");
        result1.setResult("r1");
        ScanReport result2 = new ScanReport(job2_project2, "project2");
        result2.setResult("r2");
        ScanReport result3 = new ScanReport(job3_project1, "project1");
        result3.setResult("r3");

        repositoryToTest.save(result1);
        repositoryToTest.save(result2);
        repositoryToTest.save(result3);

        /* check preconditions */
        assertEquals(3, repositoryToTest.count());
        assertNotNull(repositoryToTest.findById(job2_project2));

        /* execute */
        repositoryToTest.deleteAllReportsForProject("project1");

        /* test */
        assertEquals(1, repositoryToTest.count());
        assertNotNull(repositoryToTest.findById(job2_project2));
    }

}
