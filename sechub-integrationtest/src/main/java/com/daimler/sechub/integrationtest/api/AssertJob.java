// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

import static com.daimler.sechub.integrationtest.api.TestAPI.*;
import static org.junit.Assert.*;

import java.util.UUID;

public class AssertJob {

    public static void assertJobIsRunning(TestProject project, UUID sechubJobUUD) {
        String status;
        status = as(SUPER_ADMIN).getJobStatus(project.getProjectId(), sechubJobUUD);
        if (status.contains("ENDED") || !status.contains("STARTED") ) {
            fail ("not ENDED! status="+status);
        }
    }
    
    public static void assertJobHasNotRun(TestProject project, UUID sechubJobUUD) {
        String status =  as(SUPER_ADMIN).getJobStatus(project.getProjectId(), sechubJobUUD);
        if (status.contains("STARTED") || status.contains("ENDED") ) {
            throw new IllegalStateException("status not as expected, but:"+status);
        }
    }

    public static void assertJobHasEnded(TestProject project, UUID sechubJobUUD) {
        String status =  as(SUPER_ADMIN).getJobStatus(project.getProjectId(), sechubJobUUD);
        if (!status.contains("ENDED") || status.contains("STARTED") ) {
            throw new IllegalStateException("status not as expected, but:"+status);
        }
    }
}
