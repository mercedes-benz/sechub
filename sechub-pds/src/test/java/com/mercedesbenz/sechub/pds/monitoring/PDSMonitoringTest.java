// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.monitoring;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class PDSMonitoringTest {

    @Test
    public void monitoring_toJSON() throws Exception {
        /* prepare */
        PDSMonitoring monitoringToTest = TestPDSMonitoringDataUtil.createTestMonitoringWith2ClusterMembers();

        /* execute */
        String json = monitoringToTest.toJSON();

        /* test */
        assertNotNull(json);
    }

    @Test
    public void monitoring_example1_from_doc_can_be_loaded_and_contains_values() throws Exception {
        /* prepare */
        File file = new File("./../sechub-doc/src/docs/asciidoc/documents/pds/pds-monitoring-result-example1.json");
        String json = FileUtils.readFileToString(file, "UTF-8");

        /* execute */
        PDSMonitoring monitoring = PDSMonitoring.fromJSON(json);

        /* test */
        assertNotNull(monitoring);
        assertEquals(5, monitoring.getJobs().size());
    }

}
