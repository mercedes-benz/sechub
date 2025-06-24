// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A special future task, which calls
 * {@link PDSExecutionCallable#prepareForCancel(boolean)} before doing cancel
 * operation - so it's possible to terminate process etc. before thread is
 * interrupted
 *
 * @author Albert Tregnaghi
 *
 */
public class PDSExecutionFutureTask extends FutureTask<PDSExecutionResult> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSExecutionFutureTask.class);

    private PDSExecutionCallable execCallable;

    public PDSExecutionFutureTask(PDSExecutionCallable callable) {
        super(callable);
        execCallable = callable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {

        LOG.debug("Task starts cancellation of PDS job: {}, ", execCallable.getPdsJobUUID());

        boolean processHasTerminated = execCallable.prepareForCancel(mayInterruptIfRunning);
        boolean superImplementation = super.cancel(mayInterruptIfRunning);

        if (processHasTerminated) {
            if (superImplementation) {
                LOG.warn(
                        "This is odd: the process has been already terminated by execution callable - but the super task implementation was able to stop the future?");
            }
            return true;
        } else {
            if (superImplementation) {
                return true;
            }
        }
        return false;
    }

}
