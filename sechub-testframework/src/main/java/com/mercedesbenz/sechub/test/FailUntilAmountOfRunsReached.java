// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.core.RunOrFail;
import com.mercedesbenz.sechub.commons.core.resilience.ResilientAction;

/**
 * This is a test helper class which implements {@link RunOrFail} has a defined
 * amount of failing runs. One more run and there will be no exception thrown.
 *
 * Useful for resilience tests.
 *
 * @author Albert Tregnaghi
 *
 * @param <T>
 */
public class FailUntilAmountOfRunsReached<T extends Exception, R> implements RunOrFail<Exception>, ResilientAction<R> {

    private static final Logger LOG = LoggerFactory.getLogger(FailUntilAmountOfRunsReached.class);

    private static <T> T createThrowable(Class<T> clazzToThrow) {

        try {
            return clazzToThrow.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new IllegalStateException("The clazz to throw: " + clazzToThrow
                    + " supports no default constructor, please provide an instance in your test instead and use other constructor");
        }
    }

    private int amountOfFailingRuns;
    private T throwableToThrow;
    private int triedRunCount;
    private R resultWhenNoFailure;

    public FailUntilAmountOfRunsReached(int amountOfFailingRuns, Class<T> clazzToThrow, R resultWhenNoFailure) {
        this(amountOfFailingRuns, createThrowable(clazzToThrow), resultWhenNoFailure);
    }

    public FailUntilAmountOfRunsReached(int amountOfFailingRuns, T throwableToThrow, R resultWhenNoFailure) {
        this.amountOfFailingRuns = amountOfFailingRuns;
        this.throwableToThrow = throwableToThrow;
        this.resultWhenNoFailure = resultWhenNoFailure;
    }

    public int getTriedRunCount() {
        return triedRunCount;
    }

    @Override
    public void runOrFail() throws Exception {
        execute();
    }

    @Override
    public R execute() throws Exception {
        triedRunCount++;
        String commonPrefix = "\"Run count: {} for failing: {} - ";
        if (triedRunCount > amountOfFailingRuns) {
            LOG.debug(commonPrefix + "will not throw an exception. Will return: {} ", triedRunCount, amountOfFailingRuns, resultWhenNoFailure);
            return resultWhenNoFailure;
        }
        LOG.debug(commonPrefix + "will throw exception: {}", triedRunCount, amountOfFailingRuns, throwableToThrow.getClass().getSimpleName());
        throw throwableToThrow;
    }
}
