package com.daimler.sechub.pds.execution;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.pds.job.PDSJob;
import com.daimler.sechub.pds.job.PDSJobConfiguration;
import com.daimler.sechub.pds.job.PDSUpdateJobTransactionService;
import com.daimler.sechub.pds.job.PDSWorkspaceService;;

/**
 * Represents the callable executed inside {@link PDSExecutionFutureTask}
 * @author Albert Tregnaghi
 *
 */
class PDSExecutionCallable implements Callable<PDSExecutionResult> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionCallable.class);

    PDSJob pdsJob;
    private PDSUpdateJobTransactionService updateService;
    private Process process;

    private PDSWorkspaceService workspaceService;
    
    public PDSExecutionCallable(PDSJob pdsJob, PDSUpdateJobTransactionService upateService, PDSWorkspaceService workspaceService) {
        this.pdsJob=pdsJob;
        this.updateService=upateService;
        this.workspaceService=workspaceService;
    }

    @Override
    public PDSExecutionResult call() throws Exception {
        PDSExecutionResult result = new PDSExecutionResult();
        UUID jobUUID = pdsJob.getUUID();
        try {
            
            updateService.markJobAsRunningInOwnTransaction(jobUUID);
            
            String configJSON = pdsJob.getJsonConfiguration();
            
            PDSJobConfiguration config =PDSJobConfiguration.fromJSON(configJSON);
            
            workspaceService.unzipUploadsWhenConfigured(jobUUID, config);
            String path = workspaceService.getProductPathFor(config);
            if (path==null) {
                throw new IllegalStateException("Path not defined for product id:"+config.getProductId());
            }
            List<String> commands = new ArrayList<>();
            commands.add(path);
            /* FIXME Albert Tregnaghi, 2020-06-26: implement parameters + seperatore process exec + write tests */
            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.inheritIO();
            process = builder.start();
        }catch (Exception e) {
            LOG.error("Execution of job uuid:{} failed",jobUUID,e);
            result.failed=true;
            result.result="Job execution failed. See logs for details";
        }finally {
            try {
                workspaceService.cleanup(jobUUID);
            }catch(IOException e) {
                LOG.error("workspace cleanup failed for job:{}!",jobUUID);
            }
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