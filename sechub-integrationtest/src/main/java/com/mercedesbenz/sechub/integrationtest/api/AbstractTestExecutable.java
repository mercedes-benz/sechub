// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

public abstract class AbstractTestExecutable implements TestExecutable {

    private static final int DEFAULT_TIME_IN_MILLISECONDS_TO_WAIT_FOR_NEXT_CHECK = 300;
    private TestUser user;
    private int timeoutInSeconds;
    private boolean success;
    private Class<? extends Exception>[] handledExceptions;
    private long timeToWaitInMillis;
    private Runnable timeOutRunnable;

    @SafeVarargs
    public AbstractTestExecutable(TestUser user, int timeoutInSeconds, Class<? extends Exception>... handledExceptions) {
        this(user, timeoutInSeconds, DEFAULT_TIME_IN_MILLISECONDS_TO_WAIT_FOR_NEXT_CHECK, handledExceptions);
    }

    @SafeVarargs
    public AbstractTestExecutable(TestUser user, int timeoutInSeconds, Runnable timeOutRunnable, Class<? extends Exception>... handledExceptions) {
        this(user, timeoutInSeconds, DEFAULT_TIME_IN_MILLISECONDS_TO_WAIT_FOR_NEXT_CHECK, timeOutRunnable, handledExceptions);
    }

    @SafeVarargs
    public AbstractTestExecutable(TestUser user, int timeoutInSeconds, long timeToWaitInMillis, Class<? extends Exception>... handledExceptions) {
        this(user, timeoutInSeconds, timeToWaitInMillis, null, handledExceptions);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public AbstractTestExecutable(TestUser user, int timeoutInSeconds, long timeToWaitForNextCheckInMillis, Runnable timeOutRunnable,
            Class<? extends Exception>... handledExceptions) {
        this.user = user;
        this.timeoutInSeconds = timeoutInSeconds;
        this.timeToWaitInMillis = timeToWaitForNextCheckInMillis;

        if (handledExceptions == null) {
            this.handledExceptions = new Class[0];
        } else {
            this.handledExceptions = handledExceptions;
        }
        this.timeOutRunnable = timeOutRunnable;
    }

    public Runnable getTimeOutRunnable() {
        return timeOutRunnable;
    }

    @Override
    public TestUser getUser() {
        return user;
    }

    @Override
    public long getTimeToWaitInMillis() {
        return timeToWaitInMillis;
    }

    @Override
    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    @Override
    public Class<? extends Exception>[] getHandledExceptions() {
        return handledExceptions;
    }

    @Override
    public boolean runAndReturnTrueWhenSuccesful() throws Exception {
        boolean result = runAndReturnTrueWhenSuccesfulImpl();
        if (result) {
            success = true;
        }
        return result;
    }

    public abstract boolean runAndReturnTrueWhenSuccesfulImpl() throws Exception;

    @Override
    public boolean wasSuccessful() {
        return success;
    }
}