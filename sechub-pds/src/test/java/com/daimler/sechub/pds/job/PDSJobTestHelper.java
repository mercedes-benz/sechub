package com.daimler.sechub.pds.job;

import java.util.UUID;

public class PDSJobTestHelper {

    public static final PDSJob createTestJob(UUID uuid) {
        /* UUID is not accessible outside package ...*/
        
        PDSJob pdsJob=new PDSJob();
        pdsJob.uUID=uuid;
        return pdsJob;
    }
}
