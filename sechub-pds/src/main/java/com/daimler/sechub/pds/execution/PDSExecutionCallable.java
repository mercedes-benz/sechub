package com.daimler.sechub.pds.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.pds.job.PDSJob;
import com.daimler.sechub.pds.job.PDSUpdateJobTransactionService;;

/**
 * Represents the callable executed inside {@link PDSExecutionFutureTask}
 * @author Albert Tregnaghi
 *
 */
class PDSExecutionCallable implements Callable<PDSExecutionResult> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionCallable.class);

    PDSJob pdsJob;
    private PDSUpdateJobTransactionService markerService;
    private Process process;
    
    public PDSExecutionCallable(PDSJob pdsJob, PDSUpdateJobTransactionService markerService) {
        this.pdsJob=pdsJob;
        this.markerService=markerService;
    }

    @Override
    public PDSExecutionResult call() throws Exception {
        PDSExecutionResult result = new PDSExecutionResult();
        try {
            
            markerService.markJobAsRunningInOwnTransaction(pdsJob.getUUID());
            
            String config = pdsJob.getJsonConfiguration();
            
            
            
            
            List<String> commands = new ArrayList<>();
            /* FIXME Albert Tregnaghi, 2020-06-26: keep on implementing + write tests */
            ProcessBuilder builder = new ProcessBuilder(commands);
            process = builder.start();
        }catch (Exception e) {
            LOG.error("Execution of job uuid:{} failed",pdsJob.getUUID(),e);
            result.failed=true;
            result.result="Job execution failed. See logs for details";
        }
        
        return result;
    }

    /**
     * Is called before cancel operation on caller / task side
     * @param mayInterruptIfRunning
     */
    void prepareForCancel(boolean mayInterruptIfRunning) {
        if (process==null || ! process.isAlive()) {
            return;
        }
        LOG.info("Cancelation of process: {} will destroy underlying process forcibly");
        process.destroyForcibly();
    }

    

}