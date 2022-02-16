// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.usecase;

import com.mercedesbenz.sechub.sharedkernel.Step;

class SecHubStepDataProvider implements StepDataProvider {

    private Step step;

    public SecHubStepDataProvider(Step step) {
        this.step = step;
    }

    @Override
    public String getTitle() {
        return step.name();
    }

    @Override
    public int getNumber() {
        return step.number();
    }

    @Override
    public int[] getNext() {
        return step.next();
    }

    @Override
    public String getDescription() {
        return step.description();
    }

}