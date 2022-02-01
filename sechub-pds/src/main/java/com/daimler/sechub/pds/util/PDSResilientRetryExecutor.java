// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.util;

import static java.util.Objects.*;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDSResilientRetryExecutor<E extends Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(PDSResilientRetryExecutor.class);

    private int maxRetries;

    private Class<? extends Exception>[] handledExceptions;

    private ExceptionThrower<E> thrower;

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

    /**
     * Executes callable - with configured amount of retries for dedicated
     * exceptions. Unhandled exceptions will be wrapped inside the target
     * exceptions.
     * 
     * @param <V>
     * @param callable
     * @param identifier will be used inside target exception message to identify
     *                   the problem - e.g. could contain a job UUID
     * @return
     * @throws E
     */
    public <V> V execute(Callable<V> callable, String identifier) throws E {
        boolean done = false;

        int retries = 0;
        Exception lastCause = null;
        V result = null;

        while (!done && isRetryStillAnOption(retries)) {
            try {
                if (retries > 0) {
                    LOG.info("Start retry {} for {}", retries, identifier);
                }
                result = callable.call();
                done = true;

            } catch (Exception e) {
                if (!isExceptionRetryable(e)) {
                    thrower.throwException("A retry was not possible for " + identifier + " because exception cause was not handled.", e);
                }
                lastCause = e;
                retries++;
            }
        }

        if (!done) {
            thrower.throwException("Even after " + retries + " retries it was not possible to execute " + identifier + "! Will throw last cause.", lastCause);
        }
        return result;
    }

    protected boolean isExceptionRetryable(Exception e) {
        for (Class<? extends Exception> handledException : handledExceptions) {
            if (handledException.isAssignableFrom(e.getClass())) {
                return true;
            }
        }
        return false;
    }

    private boolean isRetryStillAnOption(int retries) {
        return retries <= maxRetries;
    }

    public interface ExceptionThrower<ET extends Exception> {

        public void throwException(String message, Exception cause) throws ET;
    }

}
