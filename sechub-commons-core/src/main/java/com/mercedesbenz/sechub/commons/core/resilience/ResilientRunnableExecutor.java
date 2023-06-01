package com.mercedesbenz.sechub.commons.core.resilience;

import com.mercedesbenz.sechub.commons.core.FailableRunnable;

public class ResilientRunnableExecutor implements ResilientExecutor<FailableRunnable<Exception>, Void> {

    ResilientActionExecutor<Void> voidActionExecutor;

    public ResilientRunnableExecutor() {
        voidActionExecutor = new ResilientActionExecutor<>();
    }

    public void add(ResilienceConsultant consultant) {
        voidActionExecutor.add(consultant);
    }

    public Void executeResilient(FailableRunnable<Exception> r, ResilienceCallback callback) throws Exception {
        return voidActionExecutor.executeResilient(() -> {
            r.runOrFail();
            return null;
        }, callback);
    }
}
