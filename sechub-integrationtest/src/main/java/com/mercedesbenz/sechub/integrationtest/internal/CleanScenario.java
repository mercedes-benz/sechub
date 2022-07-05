// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.internal;

/**
 * Those scenarios will reuse the same project names and old data will be
 * automatically deleted. Every new test inside such a scenario will first check
 * if the user, the project etc. exists and if so, drop the old data. This can
 * take some time, so using a {@link GrowingScenario} (or a
 * {@link StaticTestScenario}) is the better choice in most cases!
 *
 * @author Albert Tregnaghi
 *
 */
public interface CleanScenario extends TestScenario {

}
