// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

import java.io.IOException;

import com.mercedesbenz.sechub.commons.core.RunOrFail;

class FailDefinedTestTimes implements RunOrFail<Exception> {
    private int wantedFailTimes;
    int runs;

    FailDefinedTestTimes(int wantedFails) {
        this.wantedFailTimes = wantedFails;
    }

    @Override
    public void runOrFail() throws Exception {
        runs++;
        if (runs > wantedFailTimes) {
            System.err.println("runs: " + runs + "> wantdedFailTimes:" + wantedFailTimes);
            return;
        }
        System.out.println("failing");
        throw new IOException("From run or fail - runs: " + runs + ", wantedFailTimes: " + wantedFailTimes);
    }

}