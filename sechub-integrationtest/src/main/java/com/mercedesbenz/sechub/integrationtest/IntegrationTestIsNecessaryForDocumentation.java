// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest;

import com.mercedesbenz.sechub.test.TestIsNecessaryForDocumentation;

/**
 * Marker interface for integration tests which are necessary for documentation.
 * Via this interface we can
 * <ul>
 * <li>Identify those tests via IDE type hierarchy</li>
 * <li>Change integration test handling at build time</li>
 * <li>Provide possibility to divide/parallelize build of documentation and
 * "normal" integration tests</li>
 * </ul>
 *
 * @author Albert Tregnaghi
 *
 */
public interface IntegrationTestIsNecessaryForDocumentation extends TestIsNecessaryForDocumentation {

}
