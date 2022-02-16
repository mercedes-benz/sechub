// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

public abstract class AbstractGrowingSecHubServerTestScenario extends AbstractSecHubServerTestScenario implements GrowingScenario {

    private PersistentScenarioTestDataProvider scenarioTestDataProvider = new PersistentScenarioTestDataProvider(this);

    @Override
    protected void waitForTestDataAvailable() {
        /* we do NOT wait */

    }

    @Override
    public void grow() {
        scenarioTestDataProvider.increaseGrowId();

        /* recalculate test data... */

    }

    @Override
    public String getGrowId() {
        return scenarioTestDataProvider.getGrowId();
    }

}
