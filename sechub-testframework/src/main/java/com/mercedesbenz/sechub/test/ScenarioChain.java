// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scenario helper class for easier wiremock testing using scenarios
 *
 * @author Albert Tregnaghi
 *
 */
public class ScenarioChain {

    private static final Logger LOG = LoggerFactory.getLogger(ScenarioChain.class);

    private String scenario;
    private String stateBefore;
    private String stateAfter;

    public ScenarioChain(String name, String stateBefore) {
        this.scenario = name;
        this.stateBefore = stateBefore;
        this.stateAfter = stateBefore;
    }

    public ScenarioChain nextState(String nextState) {
        LOG.debug("switching from state:{} to next state:{}", stateBefore, nextState);
        this.stateBefore = this.stateAfter;
        this.stateAfter = nextState;
        return this;
    }

    public String getStateBefore() {
        return stateBefore;
    }

    public String getStateAfter() {
        return stateAfter;
    }

    public String getScenario() {
        return scenario;
    }
}
