// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

public interface TestExecutable {
    /**
     * @return <code>true</code> when successful (and no retry necessary), otherwise
     *         <code>false</code>
     */
    public boolean runAndReturnTrueWhenSuccesful() throws Exception;

    public boolean wasSuccessful();

    public int getTimeoutInSeconds();

    public long getTimeToWaitInMillis();

    public Class<? extends Exception>[] getHandledExceptions();

    TestUser getUser();

    /**
     * If there is a special timeout runnable defined, this will be called in case
     * of time out, before the final assert.failed is called.
     *
     * @return runnable or <code>null</code>
     */
    public Runnable getTimeOutRunnable();

}