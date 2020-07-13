// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.scenario6;

import com.daimler.sechub.integrationtest.internal.PDSTestScenario;
import com.daimler.sechub.integrationtest.internal.StaticTestScenario;

/**
 * <b><u>Scenario6 - the PDS ONLY integration test scenario</u></b><br>
 * 
 * In this scenario no sechub server instance is necessary. No special preparations are done.
 * 
 * 
 * @author Albert Tregnaghi
 *
 */
public class Scenario6 implements PDSTestScenario, StaticTestScenario {

    @Override
    public boolean isInitializationNecessary() {
        return false;
    }

    @Override
    public void prepare(String testClass, String testMethod) {
        /* no preparations necessary - just use PDS as is */
    }
}