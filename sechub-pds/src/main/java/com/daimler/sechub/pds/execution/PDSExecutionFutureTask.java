// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.execution;

import java.util.concurrent.FutureTask;

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

    private PDSExecutionCallable execCallable;

    public PDSExecutionFutureTask(PDSExecutionCallable callable) {
        super(callable);
        execCallable = callable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {

        execCallable.prepareForCancel(mayInterruptIfRunning);

        return super.cancel(mayInterruptIfRunning);
    }

}
