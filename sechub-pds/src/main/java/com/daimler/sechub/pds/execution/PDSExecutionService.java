package com.daimler.sechub.pds.execution;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.job.PDSJob;

/**
 * This class is responsible for all execution parts - it will also know what currently is happening,
 * which job is started, executed etc. But will make no changes to database
 * @author Albert Tregnaghi
 *
 */
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
    
    public void cancel(UUID jobUUID) {
        
    }

}
