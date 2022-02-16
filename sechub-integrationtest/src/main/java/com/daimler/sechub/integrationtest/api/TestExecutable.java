// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.api;

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

}