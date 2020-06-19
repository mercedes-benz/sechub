package com.daimler.sechub.pds.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.job.PDSJob;

@Service
public class PDSExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionService.class);


    public boolean isAbleToExecuteNextJob() {
        /* FIXME Albert Tregnaghi, 2020-06-19: implement */
        return true;
    }

    public void execute(PDSJob pdsJob) {
        /* FIXME Albert Tregnaghi, 2020-06-19: implement */
        LOG.debug("execute job:{}",pdsJob.getUUID());
    }

}
