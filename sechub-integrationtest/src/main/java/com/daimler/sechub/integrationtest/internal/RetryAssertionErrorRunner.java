package com.daimler.sechub.integrationtest.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.sechub.integrationtest.api.TestAPI;

/**
 * Sometimes we got race conditions in tests and an assertXYZ will fail.
 * Instead of coding always TestAPI.waitMilliSeconds(millis);  with magic time areas inside
 * testcases (which will sometimes not work, sometimes only slow down tess...), we will
 * use this class with retry mechanism.
 * @author Albert Tregnaghi
 *
 */
public class RetryAssertionErrorRunner {

    private static final Logger LOG = LoggerFactory.getLogger(RetryAssertionErrorRunner.class);
    
    /**
     * Does retry all 500 milliseconds
     * @param runnable
     * @param retries
     */
    public static void runWithRetries(int retries,Runnable runnable) {
        runWithRetries(retries, 500,runnable);
    }
    
    public static void runWithRetries(int retries,int timeToWaitBeforeFinalFail, Runnable runnable) {
        new RetryAssertionErrorRunner().start(runnable, retries, timeToWaitBeforeFinalFail);
    }
    
    private void start(Runnable runnable, int retries, int timeToWaitBeforeRetry) {
        int calls = retries+1;
        int call = 0;
        AssertionError lastAssertionFailure = null;
        for (int i=0;i<calls;i++) {
            call++;
            try {
                lastAssertionFailure=null;
                runnable.run();
            }catch(AssertionError failure) {
                LOG.warn("Call {} failed with message:{}",call,failure.getMessage());
                lastAssertionFailure=failure;
                if (i==calls-1) {
                    /* last one - so no wait */
                    continue;
                }
                LOG.warn("Call {} did fail in test - will retry in {} milliseconds {}/{} times",call,timeToWaitBeforeRetry,call,retries);
                TestAPI.waitMilliSeconds(timeToWaitBeforeRetry);
            }
        }
        if (lastAssertionFailure!=null) {
            LOG.error("Did try out {} times to run with waiting between {}  - but still problems, so throw last failure",retries,timeToWaitBeforeRetry,lastAssertionFailure);
            throw lastAssertionFailure;
        }
        
    }
    
}
