// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

/**
 * A Test scenario will prepare the scenario for testing so its "clean" for every test using this scenario!
 * Please define the test constants (e.g. TestUser) always inside their own package and use this package for the dedicated tests! So it is ensured they are not accidently used in a
 * test where another scenario is wanted (Albert did have much pain with automated imports...)
 * @author Albert Tregnaghi
 *
 */
public interface TestScenario {

	public void prepare(String testClass,String testMethod);

    public default String getName() {
        return getClass().getSimpleName().toLowerCase();
    }
}
