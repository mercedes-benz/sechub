// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Annotated test classes will only be exeucted in "regular test execution" but
 * not when only special tests shall be executed.<br>
 * <br>
 * For example: <br>
 * <br>
 * For documentation build, we need some special integration tests to be run,
 * because they gather runtime data used for documentation generation.<br>
 * So it is necessary to execute those integration tests and then the
 * documentation build in serial.
 *
 * To provide the possibility to parallelize the former mentioned steps it is
 * necessary to split the integration test execution into two parts:
 * <ol>
 * <li>"Documentation" integration tests</li>
 * <li>"Other" integration tests (regular execution)</li>
 * </ol>
 *
 * But we have also some "normal" unit tests which do not use the integration
 * test framework existing filter mechanism (see {@link IntegrationTestSetup}).
 * <br>
 * <br>
 * This means the "normal" unit tests would be executed in both calls. <br>
 * <br>
 * Normally simple unit tests are very fast and a duplicated call would not make
 * much effort, but we have some unit tests which do still need some time: 3 of
 * our Networking or SSL checks have a cumulated time of 8 seconds!
 *
 * To save execution time it becomes necessary to only execute some test at a
 * "regular test execution" situation which is automated by this annotation.
 *
 * <h3>Currently filtered (non regular) Situations by this annotation:</h3> *
 * <ul>
 * <li>When setting system property
 * {@value IntegrationTestSetup#SECHUB_INTEGRATIONTEST_ONLY_NECESSARY_TESTS_FOR_DOCUMENTATION}
 * to "true", the integration tests for documentation only will be executed by
 * {@link IntegrationTestSetup The marked tests will be always skipped.</li>
 * </ul>
 *
 * @author Albert Tregnaghi
 *
 */
@DisabledIfSystemProperty(named = IntegrationTestSupport.SECHUB_INTEGRATIONTEST_ONLY_NECESSARY_TESTS_FOR_DOCUMENTATION, matches = "true")
@Retention(RetentionPolicy.RUNTIME)
public @interface TestOnlyForRegularExecution {

}
