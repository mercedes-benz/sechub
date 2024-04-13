// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.util;

import static java.util.Objects.*;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.RunOrFail;

/**
 * An executor which tries to execute a task in a resilient way by doing a
 * defined amount of retries before finally failing. When no wait time is
 * defined (default) the executor will immediately retry the execution.
 *
 * @author Albert Tregnaghi
 *
 * @param <E> exception thrown by the executor if execution finally fails
 */
public class PDSResilientRetryExecutor<E extends Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSResilientRetryExecutor.class);

    private int maxRetries;

    private int milliSecondsToWaiBeforeRetry;

    private Class<? extends Exception>[] handledExceptions;

    private ExceptionThrower<E> thrower;

    /**
     * Creates a resilient retry executor.
     *
     * @param maxRetries        amount of retries done by the executor
     * @param thrower           the thrower which throws the final exception. The
     *                          final exception also the type for the executor.
     * @param handledExceptions an array of all exception classes which are treated
     *                          to be acceptable for a retry
     */
    @SafeVarargs
    public PDSResilientRetryExecutor(int maxRetries, ExceptionThrower<E> thrower, Class<? extends Exception>... handledExceptions) {
        requireNonNull(thrower);
        if (handledExceptions == null || handledExceptions.length == 0) {
            throw new IllegalArgumentException("At least one handled exception class must be added at constructor call");
        }
        this.thrower = thrower;
        this.maxRetries = maxRetries;
        this.handledExceptions = handledExceptions;
    }

    public void setMilliSecondsToWaiBeforeRetry(int secondsToWaitForRetry) {
        this.milliSecondsToWaiBeforeRetry = secondsToWaitForRetry;
    }

    /**
     * Executes given failable runnable - with configured amount of retries for
     * dedicated exceptions. If the execution is finally not possible, the last
     * caught execution exceptions included in the target exception and thrown by
     * this method.
     *
     * @param runnable   contains the task to execute (without a result)
     * @param identifier will be used inside target exception message to identify
     *                   the problem - e.g. could contain a job UUID
     * @throws E
     */
    public void execute(RunOrFail<?> runnable, String identifier) throws E {
        Callable<Void> callable = () -> {
            runnable.runOrFail();
            return null;
        };
        execute(callable, identifier);
    }

    /**
     * Executes given callable - with configured amount of retries for dedicated
     * exceptions. If the execution is finally not possible, the last caught
     * execution exceptions included in the target exception and thrown by this
     * method.
     *
     * @param <V>
     * @param callable   contains the task to execute
     * @param identifier will be used inside target exception message to identify
     *                   the problem - e.g. could contain a job UUID
     * @return result from task if successful
     * @throws E
     */
    public <V> V execute(Callable<V> callable, String identifier) throws E {
        boolean done = false;

        int attempts = 0;
        int retryNumber = 0;

        Exception lastCause = null;
        V result = null;

        while (!done && isRetryStillAnOption(attempts)) {
            attempts++;

            try {
                retryNumber = attempts - 1;

                if (retryNumber > 0) {
                    waitBeforeNextRetry(identifier);
                    LOG.info("Start retry {}/{} of '{}'", retryNumber, maxRetries, identifier);
                }
                result = callable.call();
                done = true;
                if (retryNumber > 0) {
                    LOG.info("The execution of '{}' was succesful", identifier);
                }

            } catch (Exception e) {
                LOG.warn("The execution of '{}' failed with '{}'. Will try to handle it resilient", identifier, e.getMessage());

                if (!isExceptionRetryable(e)) {
                    thrower.throwException("A retry of '" + identifier + "' was not possible, because exception cause was not handled.", e);
                }
                lastCause = e;
            }
        }

        if (!done) {
            LOG.warn("After {} attempts the execution of '{}' finally failed.", attempts, identifier);
            thrower.throwException("Even after " + attempts + " attempts it was not possible to execute '" + identifier + "'! Will throw last cause.",
                    lastCause);
        }
        return result;
    }

    private void waitBeforeNextRetry(String identifier) {
        if (milliSecondsToWaiBeforeRetry <= 0) {
            return;
        }

        LOG.info("Will wait {} ms before next retry of '{}'", milliSecondsToWaiBeforeRetry, identifier);
        try {
            Thread.sleep(milliSecondsToWaiBeforeRetry);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected boolean isExceptionRetryable(Exception e) {
        Class<? extends Exception> exceptionClass = e.getClass();

        for (Class<? extends Exception> handledException : handledExceptions) {
            if (handledException.isAssignableFrom(exceptionClass)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRetryStillAnOption(int attempt) {

        boolean retryStillAnOption = attempt <= maxRetries;

        LOG.trace("Retry still an option: {} for attempt: {} with max retries: {}", retryStillAnOption, attempt, maxRetries);

        return retryStillAnOption;
    }

    public interface ExceptionThrower<ET extends Exception> {

        public void throwException(String message, Exception cause) throws ET;
    }

}
