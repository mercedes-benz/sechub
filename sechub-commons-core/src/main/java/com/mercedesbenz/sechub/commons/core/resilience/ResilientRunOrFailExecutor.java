// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.resilience;

import com.mercedesbenz.sechub.commons.core.RunOrFail;

public class ResilientRunOrFailExecutor implements ResilientExecutor<RunOrFail<Exception>, Void> {

    ResilientActionExecutor<Void> voidActionExecutor;

    public ResilientRunOrFailExecutor() {
        voidActionExecutor = new ResilientActionExecutor<>();
    }

    public void add(ResilienceConsultant consultant) {
        voidActionExecutor.add(consultant);
    }

    public Void executeResilient(RunOrFail<Exception> r, ResilienceCallback callback) throws Exception {
        return voidActionExecutor.executeResilient(() -> {
            r.runOrFail();
            return null;
        }, callback);
    }
}
