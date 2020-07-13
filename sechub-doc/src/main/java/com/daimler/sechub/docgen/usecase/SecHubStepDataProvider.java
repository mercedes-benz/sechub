package com.daimler.sechub.docgen.usecase;

import com.daimler.sechub.sharedkernel.Step;

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